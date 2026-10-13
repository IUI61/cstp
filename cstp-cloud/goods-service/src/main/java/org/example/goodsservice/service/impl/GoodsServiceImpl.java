package org.examole.goodsservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.goodsservice.dto.GoodsDto;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.GoodsImage;
import org.example.goodsservice.exception.BusinessException;
import org.example.goodsservice.mapper.GoodsImageMapper;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.service.FileStorageService;
import org.example.goodsservice.service.GoodsCacheService;
import org.example.goodsservice.service.GoodsService;
import org.example.goodsservice.util.UserContext;
import org.example.goodsservice.vo.GoodsDetailVo;
import org.example.goodsservice.vo.HomeGoodsVo;
import org.example.goodsservice.vo.PageResultVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class GoodsServiceImpl implements GoodsService {
    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final GoodsCacheService goodsCacheService;
    private final FileStorageService fileStorageService;

    public GoodsServiceImpl(GoodsMapper goodsMapper,
                            GoodsImageMapper goodsImageMapper,
                            GoodsCacheService goodsCacheService,
                            FileStorageService fileStorageService) {
        this.goodsMapper = goodsMapper;
        this.goodsImageMapper = goodsImageMapper;
        this.goodsCacheService = goodsCacheService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public PageResultVo<HomeGoodsVo> handleGoodsList(int pageNum) {
        // 先取版本号快照，回填时用同一个版本号，避免写到已作废的 key 上
        long version = goodsCacheService.version();
        PageResultVo<HomeGoodsVo> cached = goodsCacheService.getPage(version, pageNum);
        if (cached != null) {
            return cached;
        }
        Page<HomeGoodsVo> page = new Page<>(pageNum, 10);
        PageResultVo<HomeGoodsVo> result = PageResultVo.of(goodsMapper.handleGoodsList(page, null));
        goodsCacheService.putPage(version, pageNum, result);
        return result;
    }

    @Override
    @Transactional
    public void goodsIssuance(MultipartFile[] file, GoodsDto goodsDto) {
        if(file == null || file.length == 0){
            throw new BusinessException(400, "图片不能为空");
        }
        // 先把全部图片校验一遍再落盘：避免"第 3 张非法时前 2 张已存成孤儿文件"
        fileStorageService.validateImages(file);
        // 先上传封面再 insert：image 才能随插入一起落库（原代码 setImage 后未 updateById，字段一直是 null）
        String coverUrl = fileStorageService.storeImage(file[0], "goods");
        Goods goods = new Goods();
        goods.setName(goodsDto.getName());
        goods.setPrice(goodsDto.getPrice());
        goods.setType(goodsDto.getType());
        goods.setDescription(goodsDto.getDescription());
        goods.setImage(coverUrl);
        goods.setUserId(UserContext.getId());
        goodsMapper.insert(goods);
        GoodsImage coverImage = new GoodsImage();
        coverImage.setGoodsId(goods.getId());
        coverImage.setUrl(coverUrl);
        coverImage.setSort(0);
        goodsImageMapper.insert(coverImage);
        for(int i = 1; i < file.length; i++){
            GoodsImage goodsImage = new GoodsImage();
            goodsImage.setGoodsId(goods.getId());
            goodsImage.setUrl(fileStorageService.storeImage(file[i], "goodsImage"));
            goodsImage.setSort(i);
            goodsImageMapper.insert(goodsImage);
        }
        goodsCacheService.invalidateList();
    }

    @Override
    @Transactional
    public GoodsDetailVo handleGoodsDetail(Long goodsId) {
        GoodsDetailVo goodsDetailVo = goodsMapper.handleGoodsDetail(goodsId);
        LambdaQueryWrapper<GoodsImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsImage::getGoodsId, goodsId).orderByAsc(GoodsImage::getSort);
        List<String> imageList = goodsImageMapper.selectList(wrapper).stream().map(GoodsImage::getUrl).toList();
        goodsDetailVo.setImageList(imageList);
        return goodsDetailVo;
    }
}
