package cn.com.wangxy.wxcp.common.model;

import lombok.Data;

@Data
public class PageQuery {
    private int pageNum = 1;
    private int pageSize = 10;
}
