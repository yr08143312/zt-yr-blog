package xyz.yangrui.ztyrblog.service;

import xyz.yangrui.ztyrblog.modal.Vo.OptionVo;

import java.util.List;
import java.util.Map;

/**
 * options的接口
 * Created by yangrui on 2019/3/7.
 */
public interface IOptionService {

    void insertOption(OptionVo optionVo);

    void insertOption(String name, String value);

    List<OptionVo> getOptions();


    /**
     * 保存一组配置
     *
     * @param options
     */
    void saveOptions(Map<String, String> options);
}
