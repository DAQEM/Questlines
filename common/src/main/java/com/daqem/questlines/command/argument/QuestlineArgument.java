package com.daqem.questlines.command.argument;

import com.daqem.arc.api.action.IAction;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.Questline;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class QuestlineArgument implements ArgumentType<Questline> {

    public static QuestlineArgument questline() {
        return new QuestlineArgument();
    }

    @Override
    public Questline parse(StringReader reader) throws CommandSyntaxException {
        return Questlines.getInstance().getQuestlineManager().getQuestline(ResourceLocation.read(reader)).orElseThrow(() -> {
            reader.setCursor(reader.getRemainingLength());
            return new CommandSyntaxException(null, Questlines.literal("Unknown questline location: " + reader.getString()), reader.getString(), reader.getCursor());
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Questlines.getInstance().getQuestlineManager().getLocationStrings(), builder);
    }

    public static Questline getQuestline(CommandContext<?> context, String name) {
        return context.getArgument(name, Questline.class);
    }
}
