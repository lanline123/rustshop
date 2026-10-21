package com.tigshop.service.setting;

import com.tigshop.bean.model.setting.AppVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.param.settings.appversion.AppVersionEditParam;
import com.tigshop.bean.param.settings.appversion.AppVersionParam;
import com.tigshop.bean.param.settings.appversion.AppVersionSaveParam;
import com.tigshop.bean.vo.setting.AppVersionDetailVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author kidd
*/
public interface AppVersionService extends IService<AppVersion> {

    /**
     * 详情
     */
    AppVersionDetailVO getDetail();

    /**
     * 添加
     */
    void create(AppVersionSaveParam param);

    /**
     * 执行更新操作
     */
    void update(AppVersionEditParam param);

    /**
     * 上传更新文件
     * @param file
     * @return
     */
    String upload(MultipartFile file);

    String getAppUpdate(AppVersionParam param);
}
