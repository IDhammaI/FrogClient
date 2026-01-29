/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package dev.idhammai.api.interfaces;

import net.minecraft.text.Text;

public interface IChatHudHook {
    public void alienClient$addMessage(Text var1, int var2);

    public void alienClient$addMessage(Text var1);

    public void alienClient$addMessageOutSync(Text var1, int var2);
}

