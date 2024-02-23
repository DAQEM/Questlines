package com.daqem.questlines.command.argument;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class ObjectiveArgument implements ArgumentType<Objective> {

    public static ObjectiveArgument objective() {
        return new ObjectiveArgument();
    }

    @Override
    public Objective parse(StringReader reader) throws CommandSyntaxException {
        return Questlines.getInstance().getQuestManager().getObjective(ResourceLocation.read(reader)).orElseThrow(() -> {
            reader.setCursor(reader.getRemainingLength());
            return new CommandSyntaxException(null, Questlines.literal("Unknown objective location: " + reader.getString()), reader.getString(), reader.getCursor());
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Questlines.getInstance().getQuestManager().getObjectiveLocationStrings(), builder);
    }

    public static Objective getObjective(CommandContext<?> context, String name) {
        return context.getArgument(name, Objective.class);
    }
}
