package org.alexdev.kepler.messages.incoming.catalogue;

import org.alexdev.kepler.dao.mysql.CurrencyDao;
import org.alexdev.kepler.dao.mysql.ItemDao;
import org.alexdev.kepler.dao.mysql.PlayerDao;
import org.alexdev.kepler.dao.mysql.TeleporterDao;
import org.alexdev.kepler.game.catalogue.*;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.ItemManager;
import org.alexdev.kepler.game.item.base.ItemBehaviour;
import org.alexdev.kepler.game.item.base.ItemDefinition;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerDetails;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.game.texts.TextsManager;
import org.alexdev.kepler.messages.outgoing.catalogue.NO_CREDITS;
import org.alexdev.kepler.messages.outgoing.rooms.items.ITEM_DELIVERED;
import org.alexdev.kepler.messages.outgoing.user.ALERT;
import org.alexdev.kepler.messages.outgoing.user.currencies.CREDIT_BALANCE;
import org.alexdev.kepler.messages.outgoing.user.currencies.FILM;
import org.alexdev.kepler.messages.outgoing.user.NO_USER_FOUND;
import org.alexdev.kepler.messages.types.MessageEvent;
import org.alexdev.kepler.server.netty.streams.NettyRequest;
import org.alexdev.kepler.util.DateUtil;
import org.alexdev.kepler.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class GRPC implements MessageEvent {
    @Override
    public void handle(Player player, NettyRequest reader) throws SQLException {
        String content = reader.contents();
        String[] data = content.split(Character.toString((char) 13));

        String saleCode = data[3];

        CatalogueItem item = CatalogueManager.getInstance().getCatalogueItem(saleCode);

        if (item == null) {
            return;
        }

        // If the item is not a buyable special rare, then check if they can actually buy it
        if (RareManager.getInstance().getCurrentRare() != null && item != RareManager.getInstance().getCurrentRare()) {
            Optional<CataloguePage> pageStream = CatalogueManager.getInstance().getCataloguePages().stream().filter(p -> p.getId() == item.getPageId()).findFirst();

            if (!pageStream.isPresent() || pageStream.get().getMinRole().getRankId() > player.getDetails().getRank().getRankId()) {
                return;
            }
        }

        if (item.getPrice() > player.getDetails().getCredits()) {
            player.send(new NO_CREDITS());
            return;
        }

        if (data[5].equals("1")) { // It's a gift!
            PlayerDetails receivingUserDetails = PlayerManager.getInstance().getPlayerData(PlayerDao.getId(data[6]));

            //if (!data[6].toLowerCase().equals(player.getDetails().getName().toLowerCase())) {
            if (receivingUserDetails == null) {
                player.send(new NO_USER_FOUND(data[6]));
                return;
            }
            //}

            String presentNote = "";
            String extraData = data[4];

            try {
                presentNote = data[7];
            } catch (Exception ignored) {
                presentNote = "";
            }

            if (presentNote.isEmpty()) {
                presentNote = " ";
            }

            Item present = new Item();
            present.setOwnerId(receivingUserDetails.getId());
            present.setDefinitionId(ItemManager.getInstance().getDefinitionBySprite("present_gen" + ThreadLocalRandom.current().nextInt(1, 7)).getId());
            present.setCustomData(saleCode +
                    (char) 9 + player.getDetails().getName() +
                    (char) 9 + StringUtil.filterInput(presentNote, true) +
                    (char) 9 + extraData +
                    (char) 9 + DateUtil.getCurrentTimeSeconds());

            ItemDao.newItem(present);

            Player receiver = PlayerManager.getInstance().getPlayerById(receivingUserDetails.getId());

            if (receiver != null) {
                receiver.getInventory().getItems().add(present);
                receiver.getInventory().getView("last");
                //receiver.send(new ITEM_DELIVERED());
            }

            player.send(new ALERT(TextsManager.getInstance().getValue("successfully_purchase_gift_for").replace("%user%", receivingUserDetails.getName())));
            //player.send(new DELIVER_PRESENT(present));
        } else {
            String extraData = null;

            if (!item.isPackage()) {
                extraData = data[4];
            }

            purchase(player, item, extraData, null, DateUtil.getCurrentTimeSeconds());
            player.getInventory().getView("new");

            boolean showItemDelivered = true;

            // Don't send item delivered if they just buy film
            if (item.getDefinition() != null && item.getDefinition().getSprite().equals("film")) {
                showItemDelivered = false;
            }

            if (showItemDelivered) {
                player.send(new ITEM_DELIVERED());
            }
        }

        CurrencyDao.decreaseCredits(player.getDetails(), item.getPrice());
        player.send(new CREDIT_BALANCE(player.getDetails()));
    }

    public static List<Item> purchase(Player player, CatalogueItem item, String extraData, String overrideName, long timestamp) throws SQLException {
       List<Item> itemsBought = new ArrayList<>();

        if (!item.isPackage()) {
            Item newItem = purchase(player, item.getDefinition(), extraData, item.getItemSpecialId(), overrideName, timestamp);

            if (newItem != null) {
                itemsBought.add(newItem);
            }
        } else {
            for (CataloguePackage cataloguePackage : item.getPackages()) {
                for (int i = 0; i < cataloguePackage.getAmount(); i++) {
                    Item newItem = purchase(player, cataloguePackage.getDefinition(), null, cataloguePackage.getSpecialSpriteId(), overrideName, timestamp);
                    itemsBought.add(newItem);
                }
            }
        }

        return itemsBought;
    }

    private static Item purchase(Player player, ItemDefinition def, String extraData, int specialSpriteId, String overrideName,  long timestamp) throws SQLException {
        // If the item is film, just give the user film
        if (def.getSprite().equals("film")) {
            CurrencyDao.increaseFilm(player.getDetails(), 5);
            player.send(new FILM(player.getDetails()));
            return null;
        }

        String customData = "";

        if (extraData != null) {
            if (def.hasBehaviour(ItemBehaviour.DECORATION)) {
                customData = extraData;
            } else {
                if (specialSpriteId > 0) {
                    customData = String.valueOf(specialSpriteId);
                }
            }

            if (def.hasBehaviour(ItemBehaviour.POST_IT)) {
                customData = "20";
            }

            if (def.hasBehaviour(ItemBehaviour.PRIZE_TROPHY)) {
                customData += (overrideName != null ? overrideName : player.getDetails().getName());
                customData += (char)9;

                customData += DateUtil.getShortDate(timestamp);
                customData += (char)9;

                customData += StringUtil.filterInput(extraData, true);
            }

            if (def.hasBehaviour(ItemBehaviour.ROOMDIMMER)) {
                customData = Item.DEFAULT_ROOMDIMMER_CUSTOM_DATA;
            }
        }

        Item item = new Item();
        item.setOwnerId(player.getDetails().getId());
        item.setDefinitionId(def.getId());
        item.setCustomData(customData);

        ItemDao.newItem(item);
        player.getInventory().getItems().add(item);

        // If the item is a camera, give them 2 free film.
        if (def.getSprite().equals("camera")) {
            CurrencyDao.increaseFilm(player.getDetails(), 2);
            player.send(new FILM(player.getDetails()));
        }

        if (def.hasBehaviour(ItemBehaviour.TELEPORTER)) {
            Item linkedTeleporterItem = new Item();
            linkedTeleporterItem.setOwnerId(player.getDetails().getId());
            linkedTeleporterItem.setDefinitionId(def.getId());
            linkedTeleporterItem.setCustomData(customData);

            ItemDao.newItem(linkedTeleporterItem);
            player.getInventory().getItems().add(linkedTeleporterItem);
            
            linkedTeleporterItem.setTeleporterId(item.getId());
            item.setTeleporterId(linkedTeleporterItem.getId());

            TeleporterDao.addPair(linkedTeleporterItem.getId(), item.getId());
            TeleporterDao.addPair(item.getId(), linkedTeleporterItem.getId());
        }

        return item;
    }
}
