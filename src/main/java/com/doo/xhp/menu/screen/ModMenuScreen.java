package com.doo.xhp.menu.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.config.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final TranslatableText HP_TEXT = new TranslatableText("xhp.menu.option.hp");
    private static final TranslatableText HEART_TEXT = new TranslatableText("xhp.menu.option.heart");
    private static final TranslatableText BAR_OR_ICON_TEXT = new TranslatableText("xhp.menu.option.bar_or_icon");
    private static final TranslatableText BAR_LENGTH_TEXT = new TranslatableText("xhp.menu.option.bar_length");
    private static final TranslatableText BAR_HEIGHT_TEXT = new TranslatableText("xhp.menu.option.bar_height");
    private static final TranslatableText DAMAGE_TEXT = new TranslatableText("xhp.menu.option.damage");
    private static final TranslatableText DISTANCE_TEXT = new TranslatableText("xhp.menu.option.distance");
    private static final TranslatableText SCALE_TEXT = new TranslatableText("xhp.menu.option.scale");
    private static final TranslatableText HEIGHT_TEXT = new TranslatableText("xhp.menu.option.height");

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private Screen pre;

    private ModMenuScreen() {
        super(new LiteralText(XHP.ID));
    }

    @Override
    protected void init() {
        // 显示hp
        int buttonX = this.width / 2 - 150 / 2;
        int buttonY = 20;
        int count = 1;
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                HP_TEXT.copy().append(": " + XHP.option.hp),
                b -> b.setMessage(HP_TEXT.copy().append(": " + XHP.option.clickHp()))));
        // 显示血量
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                HEART_TEXT.copy().append(": " + XHP.option.heart),
                b -> b.setMessage(HEART_TEXT.copy().append(": " + XHP.option.clickHeart()))));
        // 显示barOrIcon
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                BAR_OR_ICON_TEXT.copy().append(": " + XHP.option.barOrIcon),
                b -> b.setMessage(BAR_OR_ICON_TEXT.copy().append(": " + XHP.option.clickBarOrIcon()))));
        // 显示bar长度
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                BAR_LENGTH_TEXT.copy().append(": " + XHP.option.barLength),
                b -> b.setMessage(BAR_LENGTH_TEXT.copy().append(": " + XHP.option.clickBarLength()))));
        // 显示bar高度
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                BAR_HEIGHT_TEXT.copy().append(": " + XHP.option.barHeight),
                b -> b.setMessage(BAR_HEIGHT_TEXT.copy().append(": " + XHP.option.clickBarHeight()))));
        // 显示伤害
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                DAMAGE_TEXT.copy().append(": " + XHP.option.damage),
                b -> b.setMessage(DAMAGE_TEXT.copy().append(": " + XHP.option.clickDamage()))));
        // 显示默认距离
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                DISTANCE_TEXT.copy().append(": " + XHP.option.distance),
                b -> b.setMessage(DISTANCE_TEXT.copy().append(": " + XHP.option.clickDistance()))));
        // 显示缩放比例
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                SCALE_TEXT.copy().append(": " + String.format("%.3f", XHP.option.scale)),
                b -> b.setMessage(SCALE_TEXT.copy().append(": " + String.format("%.3f", XHP.option.clickScale())))));
        // 显示基础高度
        this.addButton(new ButtonWidget(buttonX, buttonY * count++, 150, 20,
                HEIGHT_TEXT.copy().append(": " + XHP.option.height),
                b -> b.setMessage(HEIGHT_TEXT.copy().append(": " + XHP.option.clickHeight()))));
        // 返回按钮
        this.addButton(new ButtonWidget(buttonX, this.height - buttonY, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
        Config.LOGGER.log(Level.INFO, "加载了{}个按键(add {} button)", count, count);
    }

    public static ModMenuScreen get(Screen pre) {
        INSTANCE.width = pre.width;
        INSTANCE.height = pre.height;
        INSTANCE.pre = pre;
        return INSTANCE;
    }

    private void close() {
        if (client != null) {
            // 返回上个页面
            client.currentScreen = this.pre;
            // 保存设置的配置
            Config.write(XHP.ID, XHP.option);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // 画背景
        super.renderBackground(matrices);
        // 画其他
        super.render(matrices, mouseX, mouseY, delta);
    }
}
