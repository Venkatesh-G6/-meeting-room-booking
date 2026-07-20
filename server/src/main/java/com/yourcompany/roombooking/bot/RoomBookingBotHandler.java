package com.yourcompany.roombooking.bot;

import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ChannelAccount;
import com.yourcompany.roombooking.dto.response.SimulateResponse;
import com.yourcompany.roombooking.service.BookingService;
import com.yourcompany.roombooking.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/*
 * TeamsBot processes all incoming
 * Teams messages.
 * In dev mode tested via simulation UI.
 * In prod mode called via /api/messages
 * endpoint from Azure Bot Service.
 *
 * Multi-turn dialog support via
 * ConversationStateManager and
 * MultiTurnDialogHandler.
 */
@Component
public class RoomBookingBotHandler extends ActivityHandler {

    private static final Logger log = LoggerFactory.getLogger(RoomBookingBotHandler.class);

    private static final String ADAPTIVE_CARD_CONTENT_TYPE = "application/vnd.microsoft.card.adaptive";

    private final BookingService bookingService;
    private final RoomService roomService;
    private final ConversationStateManager stateManager;
    private final MultiTurnDialogHandler multiTurnHandler;

    public RoomBookingBotHandler(BookingService bookingService,
                                 RoomService roomService,
                                 ConversationStateManager stateManager,
                                 MultiTurnDialogHandler multiTurnHandler) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.stateManager = stateManager;
        this.multiTurnHandler = multiTurnHandler;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();
        String text = activity.getText();
        String userId = activity.getFrom() != null ? activity.getFrom().getId() : "unknown";
        String userEmail = userId;

        log.info("Processing message from {}: {}", userEmail, text);

        ConversationContext context = stateManager.getContext(userId);
        context.setUserEmail(userEmail);

        SimulateResponse response = multiTurnHandler.handleMessage(text, userEmail, context);

        stateManager.saveContext(userId, context);

        if (context.getStep() == ConversationStep.IDLE) {
            stateManager.clearContext(userId);
        }

        if (response.getCardJson() != null) {
            return sendCard(turnContext, response.getCardJson());
        } else {
            return turnContext.sendActivity(response.getMessage())
                    .thenApply(resourceResponse -> null);
        }
    }

    public SimulateResponse processMessage(String text, String userEmail) {
        log.info("Processing message from {}: {}", userEmail, text);

        String userId = userEmail;
        ConversationContext context = stateManager.getContext(userId);
        context.setUserEmail(userEmail);

        SimulateResponse response = multiTurnHandler.handleMessage(text, userEmail, context);

        stateManager.saveContext(userId, context);

        if (context.getStep() == ConversationStep.IDLE) {
            stateManager.clearContext(userId);
        }

        return response;
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return sendCard(turnContext, AdaptiveCardBuilder.buildHelpCard());
    }

    private CompletableFuture<Void> sendCard(TurnContext turnContext, String cardJson) {
        Attachment attachment = new Attachment();
        attachment.setContentType(ADAPTIVE_CARD_CONTENT_TYPE);
        attachment.setContent(cardJson);

        Activity reply = Activity.createMessageActivity();
        reply.setAttachments(new ArrayList<>(List.of(attachment)));

        return turnContext.sendActivity(reply).thenApply(resourceResponse -> null);
    }
}
