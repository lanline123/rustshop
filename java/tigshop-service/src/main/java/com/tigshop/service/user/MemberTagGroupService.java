package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.user.MemberTagGroup;
import com.tigshop.bean.param.user.MemberTagGroupCreateParam;
import com.tigshop.bean.param.user.MemberTagGroupQueryPage;
import com.tigshop.bean.param.user.MemberTagGroupUpdateParam;
import com.tigshop.bean.vo.user.MemberTagGroupDetailVO;
import com.tigshop.bean.vo.user.MemberTagGroupListVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.Valid;

public interface MemberTagGroupService extends BaseService<MemberTagGroup> {

    Page<MemberTagGroupListVO> list(MemberTagGroupQueryPage queryPage);

    MemberTagGroupDetailVO detail(Long id);

    void create(@Valid MemberTagGroupCreateParam param);

    void update(@Valid MemberTagGroupUpdateParam param);

    void delGroup(Long id);

    void setTop(Long id);

    void cancelTop(Long id);
}
