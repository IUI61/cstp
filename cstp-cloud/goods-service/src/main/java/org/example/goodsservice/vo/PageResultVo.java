package org.example.goodsservice.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResultVo<T> {
    private List<T> records;
    private long total;
    private long pageNum;
    private long size;

    // 返回带泛型的 PageResultVo<T>，避免 raw type 引起的编译告警
    public static <T> PageResultVo<T> of(IPage<T> page) {
        PageResultVo<T> result = new PageResultVo<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }
}
