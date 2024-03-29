package com.gpower.common.ueditor;

import com.gpower.common.ueditor.define.ActionMap;
import com.gpower.common.ueditor.define.AppInfo;
import com.gpower.common.ueditor.define.BaseState;
import com.gpower.common.ueditor.define.State;
import com.gpower.common.ueditor.hunter.ImageHunter;
import com.gpower.common.ueditor.manager.IUeditorFileManager;
import com.gpower.common.ueditor.upload.Uploader;
import com.gpower.common.utils.JsonUtils;
import com.gpower.common.utils.WorkSpaceConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class UeditorService {

    @Autowired
    private UeditorManager ueditorManager;

    public String exec(HttpServletRequest request) {
        String callbackName = request.getParameter("callback");
        if (callbackName != null) {
            if (!validCallbackName(callbackName)) {
                return new BaseState(false, AppInfo.ILLEGAL).toJSONString();
            }
            return callbackName + "(" + invoke(request) + ");";
        } else {
            return invoke(request);
        }
    }

    private String invoke(HttpServletRequest request) {
        String actionType = request.getParameter("action");
        // String rootPath = request.getServletContext().getRealPath("/");
        String rootPath = WorkSpaceConstants.getUploadFileUeditorPath();
        String ctxPath = request.getContextPath();

        if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
            return new BaseState(false, AppInfo.INVALID_ACTION).toJSONString();
        }
        if (ueditorManager == null || !ueditorManager.valid()) {
            return new BaseState(false, AppInfo.CONFIG_ERROR).toJSONString();
        }

        IUeditorFileManager fileManager = ueditorManager.getFileManager();

        State state = null;
        int actionCode = ActionMap.getType(actionType);
        ActionConfig conf = null;

        switch (actionCode) {

            case ActionMap.CONFIG:
                UeditorConfig allConfig = ueditorManager.getConfig();
                String imageUrlPrefix = allConfig.getImageUrlPrefix();
                String scrawlUrlPrefix = allConfig.getScrawlUrlPrefix();
                String snapscreenUrlPrefix = allConfig.getSnapscreenUrlPrefix();
                String catcherUrlPrefix = allConfig.getCatcherUrlPrefix();
                String videoUrlPrefix = allConfig.getVideoUrlPrefix();
                String fileUrlPrefix = allConfig.getFileUrlPrefix();
                String imageManagerUrlPrefix = allConfig.getImageManagerUrlPrefix();
                String fileManagerUrlPrefix = allConfig.getFileManagerUrlPrefix();

                if (StringUtils.isBlank(imageUrlPrefix)) {
                    allConfig.setImageUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(scrawlUrlPrefix)) {
                    allConfig.setScrawlUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(snapscreenUrlPrefix)) {
                    allConfig.setSnapscreenUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(catcherUrlPrefix)) {
                    allConfig.setCatcherUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(videoUrlPrefix)) {
                    allConfig.setVideoUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(fileUrlPrefix)) {
                    allConfig.setFileUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(imageManagerUrlPrefix)) {
                    allConfig.setImageManagerUrlPrefix(ctxPath);
                }
                if (StringUtils.isBlank(fileManagerUrlPrefix)) {
                    allConfig.setFileManagerUrlPrefix(ctxPath);
                }
                return JsonUtils.toJson(allConfig);

            case ActionMap.UPLOAD_IMAGE:
            case ActionMap.UPLOAD_SCRAWL:
            case ActionMap.UPLOAD_VIDEO:
            case ActionMap.UPLOAD_FILE:
                conf = ueditorManager.getConfig(actionCode, rootPath);    // 获取配置
                state = new Uploader(request, conf).doExec(fileManager);    // 下载文件到服务器
                break;

            case ActionMap.CATCH_IMAGE:
                conf = ueditorManager.getConfig(actionCode, rootPath);
                String[] list = request.getParameterValues(conf.getFieldName());
                state = new ImageHunter(fileManager, conf).capture(list);
                break;

            case ActionMap.LIST_IMAGE:
            case ActionMap.LIST_FILE:
                conf = ueditorManager.getConfig(actionCode, rootPath);
                int start = getStartIndex(request);
                state = fileManager.list(conf, start);
                break;

        }
        return state.toJSONString();
    }

    public int getStartIndex(HttpServletRequest request) {
        String start = request.getParameter("start");
        try {
            return Integer.parseInt(start);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * callback参数验证
     *
     * @param name 参数名
     * @return boolean 是否校验通过
     */
    public boolean validCallbackName(String name) {
        return name.matches("^[a-zA-Z_]+[\\w0-9_]*$");
    }
}
