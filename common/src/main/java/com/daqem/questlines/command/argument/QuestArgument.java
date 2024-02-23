package com.daqem.questlines.command.argument;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.quest.Quest;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class QuestArgument implements ArgumentType<Quest> {

    public static QuestArgument quest() {
        return new QuestArgument();
    }

    @Override
    public Quest parse(StringReader reader) throws CommandSyntaxException {
        return Questlines.getInstance().getQuestManager().getQuest(ResourceLocation.read(reader)).orElseThrow(() -> {
            reader.setCursor(reader.getRemainingLength());
            return new CommandSyntaxException(null, Questlines.literal("Unknown quest location: " + reader.getString()), reader.getString(), reader.getCursor());
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Questlines.getInstance().getQuestManager().getLocationStrings(), builder);
    }

    public static Quest getQuest(CommandContext<?> context, String name) {
        return context.getArgument(name, Quest.class);
    }
}
