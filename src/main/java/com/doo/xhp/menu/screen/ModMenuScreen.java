package com.doo.xhp.menu.screen;

import com.doo.xhp.XHP;
import com.doo.xhp.config.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

/**
 * mod menu 配置界面
 */
public class ModMenuScreen extends Screen {

    private static final TranslatableText HP_TEXT = new TranslatableText("xhp.menu.option.hp");
    private static final TranslatableText HEART_TEXT = new TranslatableText("xhp.menu.option.heart");
    private static final TranslatableText DAMAGE_TEXT = new TranslatableText("xhp.menu.option.damage");

    private static final ModMenuScreen INSTANCE = new ModMenuScreen();

    private Screen pre;

    private ModMenuScreen() {
        super(new LiteralText(XHP.ID));
        init();
    }

    @Override
    protected void init() {
        // 显示hp
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, 28, 150, 20,
                HP_TEXT.copy().append(": " + XHP.option.hp),
                b -> b.setMessage(HP_TEXT.copy().append(": " + XHP.option.clickHp()))));
        // 显示血量
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, 28 * 2, 150, 20,
                HEART_TEXT.copy().append(": " + XHP.option.heart),
                b -> b.setMessage(HEART_TEXT.copy().append(": " + XHP.option.clickHeart()))));
        // 显示伤害
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, 28 * 3, 150, 20,
                DAMAGE_TEXT.copy().append(": " + XHP.option.damage),
                b -> b.setMessage(DAMAGE_TEXT.copy().append(": " + XHP.option.clickDamage()))));
        // 返回按钮
        this.addButton(new ButtonWidget(this.width / 2 - 150 / 2, this.height - 28, 150, 20,
                ScreenTexts.BACK, b -> INSTANCE.close()));
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
