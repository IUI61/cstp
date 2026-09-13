package org.example.cstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cstp.entity.Goods;
import org.example.cstp.entity.GoodsImage;
import org.example.cstp.exception.BusinessException;
import org.example.cstp.mapper.GoodsImageMapper;
import org.example.cstp.mapper.GoodsMapper;
import org.example.cstp.service.GoodsService;
import org.example.cstp.util.FileUtil;
import org.example.cstp.vo.GoodsDetailVo;
import org.example.cstp.vo.HomeGoodsVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;


@Service
public class GoodsServiceImpl implements GoodsService {
    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final GoodsImageMapper goodsImageMapper;
    private final ObjectMapper objectMapper;

    public GoodsServiceImpl(GoodsMapper goodsMapper,
                            GoodsImageMapper goodsImageMapper,
                            StringRedisTemplate stringRedisTemplate,
                            ObjectMapper objectMapper) {
        this.goodsMapper = goodsMapper;
        this.goodsImageMapper = goodsImageMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public IPage<HomeGoodsVo> handleGoodsList(int pageNum) {
        String key = "goods:page:" + pageNum;
        // 1. 查缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<Page<HomeGoodsVo>>() {});
            } catch (JsonProcessingException e) {
                throw new BusinessException(500, "缓存数据异常");
            }
        }
        // 2. 查数据库
        Page<HomeGoodsVo> page = new Page<>(pageNum, 10);
        IPage<HomeGoodsVo> result = goodsMapper.handleGoodsList(page, null);
        // 3. 写缓存
        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofMinutes(30));
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "序列化失败");
        }
        return result;
    }

    @Override
    @Transactional
    public void goodsIssuance(MultipartFile[] file, Goods goods) {
        if(file == null || file.length == 0){
            throw new BusinessException(400, "图片不能为空");
        }
        Goods newGoods = goods;
        goodsMapper.insert(newGoods);
        String coverUrl = FileUtil.imageUpload(file[0], "goods");
        newGoods.setImage(coverUrl);
        GoodsImage coverImage = new GoodsImage();
        coverImage.setGoodsId(newGoods.getId());
        coverImage.setUrl(coverUrl);
        coverImage.setSort(0);
        goodsImageMapper.insert(coverImage);
        for(int i = 1; i < file.length; i++){
            GoodsImage goodsImage = new GoodsImage();
            goodsImage.setGoodsId(goods.getId());
            goodsImage.setUrl(FileUtil.imageUpload(file[i], "goodsImage"));
            goodsImage.setSort(i);
            goodsImageMapper.insert(goodsImage);
        }
        stringRedisTemplate.delete("goods:page:1");
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
