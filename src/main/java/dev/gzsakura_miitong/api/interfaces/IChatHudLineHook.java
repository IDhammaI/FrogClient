/*
 * Decompiled with CFR 0.152.
 */
package dev.gzsakura_miitong.api.interfaces;

import dev.gzsakura_miitong.api.utils.math.FadeUtils;

public interface IChatHudLineHook {
    public int alienClient$getMessageId();

    public void alienClient$setMessageId(int var1);

    public boolean alienClient$getSync();

    public void alienClient$setSync(boolean var1);

    public FadeUtils alienClient$getFade();

    public void alienClient$setFade(FadeUtils var1);
}

