// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.RegionCreateDTO;
import com.tigshop.bean.dto.setting.RegionUpdateDTO;
import com.tigshop.bean.dto.setting.RegionListDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.setting.RegionListVO;
import com.tigshop.bean.vo.sys.CityRegionVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.mapper.setting.RegionMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tigshop.common.constant.Constants.CITY_PINYING_LIST;
import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.utils.Ip2RegionUtil.getRegionByIp;

/**
 * 地区列表服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
@Service
public class RegionServiceImpl extends BaseServiceImpl<RegionMapper, Region> implements RegionService {

    private static final int CHONGQING_CITY_REGION_ID = 500100;
    private static final Set<Integer> DEPRECATED_CHONGQING_DISTRICT_IDS = Set.of(500105, 500112);

    @Resource
    private ConfigService configService;
    @Resource
    private RedisCache redisCache;
    @Autowired
    private TigshopProperties tigshopProperties;

    @Override
    public List<Region> getRegionByParentId(Integer parentId) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        if (parentId == null) {
            throw new GlobalException(PARAM_ERROR);
        }
        queryWrapper.eq(Region::getParentId, parentId);
        List<Region> result = sortByFirstWord(filterSelectableRegions(parentId, this.list(queryWrapper), null));
        return result;
    }


    /**
     * 地区列表排序：严格以后台「首字母」列（first_word）为准升序排列，未设置首字母的排到最后；
     * 首字母相同（或同为未设置）时，再按地区名字的全拼字母排序
     *
     * @param regions 待排序地区列表
     * @return 排序后的地区列表
     */
    private List<Region> sortByFirstWord(List<Region> regions) {
        if (regions == null || regions.isEmpty()) {
            return regions;
        }
        return regions.stream()
                .sorted(Comparator
                        .comparing(this::resolveFirstWord, Comparator.nullsLast(String::compareTo))
                        .thenComparing(this::resolveFullPinyin, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * 新地址选择不再展示已废止的原江北区、原渝北区；加载存量地址时仍回填当前选中的旧节点，
     * 使历史地址可以正常展示并由用户重新选择。
     */
    static List<Region> filterSelectableRegions(Integer parentId, List<Region> regions, Integer selectedRegionId) {
        if (!Objects.equals(parentId, CHONGQING_CITY_REGION_ID) || regions == null || regions.isEmpty()) {
            return regions;
        }
        return regions.stream()
                .filter(region -> !DEPRECATED_CHONGQING_DISTRICT_IDS.contains(region.getRegionId())
                        || Objects.equals(region.getRegionId(), selectedRegionId))
                .toList();
    }

    /**
     * 解析地区首字母：仅取后台设置的 first_word；未设置时返回 null，排序时排到最后
     */
    private String resolveFirstWord(Region region) {
        String firstWord = region.getFirstWord();
        if (StrUtil.isNotBlank(firstWord)) {
            return firstWord.trim().toUpperCase();
        }
        return null;
    }

    /**
     * 解析地区名字的全拼，用于首字母相同时的二级排序
     */
    private String resolveFullPinyin(Region region) {
        String regionName = region.getRegionName();
        if (StrUtil.isBlank(regionName)) {
            return "";
        }
        return PinyinUtil.getPinyin(regionName, "").toLowerCase();
    }

    @Override
    public List<String> getRegionNamesByRegionIds(List<Integer> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Region::getRegionId, regionIds);
        List<Region> regions = this.list(queryWrapper);
        return regions.stream().map(Region::getRegionName).toList();
    }

    @Override
    public List<Tree<Integer>> getAllRegionTree() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        if (tigshopProperties.getIsOverseas() == 1) {
            queryWrapper.ge(Region::getLevel, 0);
        } else {
            queryWrapper.gt(Region::getLevel, 0);
        }
        List<Region> regionsList = this.list(queryWrapper);
        List<Tree<Integer>> regionTree = getRegionTree(regionsList);
        return regionTree;
    }

    /**
     * 获取地区列表<树形结构>
     *
     * @param regionList 地区列表
     * @return List<Tree < Integer>>
     */
    private List<Tree<Integer>> getRegionTree(List<Region> regionList) {
        if (regionList.isEmpty()) {
            return Collections.emptyList();
        }

        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("regionId");
        config.setParentIdKey("parentId");
        config.setNameKey("regionName");
        config.setDeep(8);

        return TreeUtil.build(regionList, 0, config, (object, treeNode) -> {
            treeNode.putExtra("regionId", object.getRegionId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("regionName", object.getRegionName());
        });
    }

    @Override
    public Page<RegionListVO> list(RegionListDTO dto) {
        // 分页查询
        Page<Region> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        Integer parentId = dto.getParentId();
        if (parentId != null && parentId >= 0) {
            queryWrapper.eq(Region::getParentId, parentId);
        }
        Integer regionId = dto.getRegionId();
        if (regionId != null && regionId >= 0) {
            queryWrapper.eq(Region::getRegionId, regionId);
        }
        // 构建排序条件
        if (StrUtil.isBlank(dto.getSortField())) {
            queryWrapper.orderByAsc(Region::getSortOrder).orderByAsc(Region::getRegionId);
        } else {
            buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        }
        // 构建查询条件
        Page<Region> regionPage = this.page(page, queryWrapper);
        List<Region> regions = regionPage.getRecords();
        // 将查询结果转为 VO 返回
        List<RegionListVO> regionListVOList = regions.stream().map(region -> {
            RegionListVO regionListVO = new RegionListVO();
            BeanUtils.copyProperties(region, regionListVO);
            return regionListVO;
        }).toList();
        return PageUtil.convertPage(regionPage, regionListVOList);
    }

    @Override
    public boolean create(RegionCreateDTO dto) {
        Region region = new Region();
        BeanUtils.copyProperties(dto, region);
        return this.save(region);
    }

    @Override
    public boolean updateRegion(RegionUpdateDTO dto) {
        Region region = this.getById(dto.getId());
        if (region == null) {
            throw new GlobalException("地区不存在");
        }
        Integer parentId = dto.getParentId();
        if (parentId == null) {
            throw new GlobalException("上级地区不能为空");
        }
        if (Objects.equals(parentId, dto.getId())) {
            throw new GlobalException("上级地区不能选择自身");
        }
        Integer level = 1;
        if (parentId != 0) {
            Region parent = this.getById(parentId);
            if (parent == null || parent.getLevel() == null) {
                throw new GlobalException("上级地区不存在");
            }
            level = parent.getLevel() + 1;
        }
        region.setRegionName(dto.getRegionName());
        region.setParentId(parentId);
        region.setLevel(level);
        region.setIsHot(dto.getIsHot() == null ? 0 : dto.getIsHot());
        region.setSortOrder(dto.getSortOrder() == null ? 50 : dto.getSortOrder());
        String firstWord = StrUtil.trim(dto.getFirstWord());
        region.setFirstWord(StrUtil.isBlank(firstWord)
                ? PinyinUtil.getPinyin(dto.getRegionName()).substring(0, 1).toUpperCase()
                : firstWord.substring(0, 1).toUpperCase());
        return this.updateById(region);
    }

    private String normalizeStandardCode(String value) {
        return StrUtil.isBlank(value) ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public List<List<Region>> getRegionByIds(String regionIds) {
        List<List<Region>> regionList = new ArrayList<>();
        //string转json
        ConfigPO defaultCountryCode = configService.getConfigByCode(SettingsEnum.DEFAULT_COUNTRY.getBizCode());
        Integer defaultCountry = Integer.valueOf(defaultCountryCode.getBizVal());

        List<Integer> regionIdsList;
        if (regionIds != null) {
            regionIdsList = Arrays.stream(regionIds.split(",")).map(Integer::parseInt).toList();
            Region parentRegion = this.getById(regionIdsList.getFirst());
            List<Integer> parentList = new ArrayList<>();
            if (parentRegion != null) {
                parentList.add(parentRegion.getParentId());
                parentList.addAll(regionIdsList);
                regionIdsList = parentList;
            }

        } else {
            regionIdsList = new ArrayList<>();
            regionIdsList.add(defaultCountry);
        }
        if (!regionIdsList.isEmpty()) {
            for (int i = 0; i < regionIdsList.size(); i++) {
                Integer parentId = regionIdsList.get(i);
                Integer selectedRegionId = i + 1 < regionIdsList.size() ? regionIdsList.get(i + 1) : null;
                List<Region> children = this.list(new LambdaQueryWrapper<Region>().eq(Region::getParentId, parentId));
                regionList.add(sortByFirstWord(filterSelectableRegions(parentId, children, selectedRegionId)));
            }
        }
        return regionList;
    }

    @Override
    public List<Region> getProvinceList() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, 2)
                .select(Region::getRegionId, Region::getRegionName, Region::getFirstWord);
        List<Region> provinceList = this.list(queryWrapper).stream().peek(region -> {
            String regionName = region.getRegionName();
            region.setRegionName(regionName.replace("省", "").replace("市", "").replace("自治区", ""));
        }).toList();
        return sortByFirstWord(provinceList);
    }

    @Override
    public Region getUserRegion() {
        try {
            String region = "";
            try{
                region = getRegionByIp();
                if(Objects.equals("0",region)){
                    //内部地址
                    region = null;
                }
            }catch (Throwable e){

            }
            region = StrUtil.isBlank(region) ? "北京" : region;
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Region::getRegionName, region);
            return this.list(queryWrapper).getFirst();
        } catch (Exception e) {
            return this.list().getFirst();
        }
    }

    @Override
    public CityRegionVO getCityRegion() {
        Object cacheObject = redisCache.getCacheObject(CITY_PINYING_LIST);
        if (cacheObject != null) {
            return JSONUtil.toBean(cacheObject.toString(), CityRegionVO.class);
        }
        List<Region> hot = list(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, 3)
                .eq(Region::getIsHot, 1)
        );
        List<Region> list = list(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, 3)
                .orderByAsc(Region::getFirstWord)
                .orderByAsc(Region::getRegionId)
        );
        CityRegionVO cityRegionVO = new CityRegionVO();
        cityRegionVO.setHot(hot);
        cityRegionVO.setCity(list);
        redisCache.setCacheObject(CITY_PINYING_LIST, JSONUtil.toJsonStr(cityRegionVO));
        return cityRegionVO;
    }

    @Override
    public List<Region> getCityListByCityName(String cityName) {
        return list(new LambdaQueryWrapper<Region>()
                .like(Region::getRegionName, cityName)
                .eq(Region::getLevel, 3)
        );
    }

    @Override
    public List<Region> getRegionPath(Integer regionId) {
        if (regionId == null) {
            throw new GlobalException(PARAM_ERROR);
        }

        List<Region> path = new ArrayList<>();
        Integer currentId = regionId;

        // 获取默认国家配置
        ConfigPO defaultCountryCode = configService.getConfigByCode(SettingsEnum.DEFAULT_COUNTRY.getBizCode());
        Integer defaultCountryId = null;
        if (defaultCountryCode != null && StrUtil.isNotBlank(defaultCountryCode.getBizVal())) {
            defaultCountryId = Integer.valueOf(defaultCountryCode.getBizVal());
        }

        // 向上遍历获取完整路径
        while (currentId != null && currentId != 0) {
            Region region = this.getById(currentId);
            if (region == null) {
                break;
            }

            path.add(region);

            // 如果设置了默认国家ID且当前节点就是默认国家，则停止遍历
            if (defaultCountryId != null && currentId.equals(defaultCountryId)) {
                break;
            }

            currentId = region.getParentId();
        }

        // 如果没有设置默认国家或者路径中不包含默认国家，确保路径包含根节点（中国）
        if (defaultCountryId == null && !path.isEmpty()) {
            // 继续向上找到根节点
            while (currentId != null && currentId != 0) {
                Region region = this.getById(currentId);
                if (region == null) {
                    break;
                }
                path.add(region);
                currentId = region.getParentId();
            }
        }

        // 反转列表，使其从根节点到叶子节点排列
        Collections.reverse(path);
        return path;
    }
}
