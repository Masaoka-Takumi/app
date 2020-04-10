package jp.pioneer.carsync.presentation.view;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;

public interface AlexaDisplayCardView {
    /**
     * ダイアログ終了
     */
    void callbackClose();
    void closeDialogWithAnimation();
    void setTemplate(final RenderTemplateItem renderTemplateItem);
}
