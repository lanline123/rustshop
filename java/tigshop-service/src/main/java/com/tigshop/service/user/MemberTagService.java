package com.tigshop.service.user;

import com.tigshop.bean.model.user.MemberTag;
import com.tigshop.bean.param.user.MemberTagCreateParam;
import com.tigshop.bean.param.user.MemberTagUpdateParam;
import com.tigshop.bean.vo.user.MemberTagVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.Valid;

import java.util.List;

public interface MemberTagService extends BaseService<MemberTag> {

    void create(@Valid MemberTagCreateParam param);

    void update(@Valid MemberTagUpdateParam param);

    void delTag(Long id);

    List<MemberTagVO> listByGroupId(Long groupId);
}
