package net.tacobuddies.bot;

import net.runelite.client.externalplugins.ExternalPluginManager;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new AssertionTransformer());
        inst.addTransformer(new LauncherTransformer());

        try {
            ExternalPluginManager.loadBuiltin(Bot.class);
        } catch(NoClassDefFoundError ignored) {}
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new AssertionTransformer());
        inst.addTransformer(new LauncherTransformer());

        try {
            ExternalPluginManager.loadBuiltin(Bot.class);
        } catch(NoClassDefFoundError ignored) {}
    }
}
