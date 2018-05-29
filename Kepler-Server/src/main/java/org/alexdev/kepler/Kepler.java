package org.alexdev.kepler;

import io.netty.util.ResourceLeakDetector;
import org.alexdev.kepler.dao.Storage;
import org.alexdev.kepler.game.GameScheduler;
import org.alexdev.kepler.game.catalogue.CatalogueManager;
import org.alexdev.kepler.game.commands.CommandManager;
import org.alexdev.kepler.game.item.ItemManager;
import org.alexdev.kepler.game.moderation.FuserightsManager;
import org.alexdev.kepler.game.navigator.NavigatorManager;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.game.room.RoomManager;
import org.alexdev.kepler.game.room.models.RoomModelManager;
import org.alexdev.kepler.game.room.public_rooms.walkways.WalkwaysManager;
import org.alexdev.kepler.game.texts.TextsManager;
import org.alexdev.kepler.messages.MessageHandler;
import org.alexdev.kepler.server.netty.NettyServer;
import org.alexdev.kepler.util.config.GameConfiguration;
import org.alexdev.kepler.util.config.ServerConfiguration;
import org.alexdev.kepler.util.DateUtil;
import org.alexdev.kepler.util.config.LoggingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;

public class Kepler {

    private static long startupTime;

    private static String serverIP;
    private static int serverPort;

    private static boolean isShutdown;
    
    private static NettyServer server;
    private static Logger log;

    /**
     * Main call of Java application
     * @param args System arguments
     */
    public static void main(String[] args) {
        startupTime = DateUtil.getCurrentTimeSeconds();

        try {
            LoggingConfiguration.checkLoggingConfig();

            ServerConfiguration.load("server.ini");
            GameConfiguration.load("game.ini");

            log = LoggerFactory.getLogger(Kepler.class);
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

            // TODO: The "Standard" ASCII from
            // http://patorjk.com/software/taag/#p=display&f=Standard&t=Kepler
            // On a grey background with white ASCII text
            // ASCII logo on the left side, contributors and other credits/info on the right side
            // TODO: also a way to disable the fancy headers, as in production it would only fill the logs
            log.info("Kepler - Habbo Hotel V21 Emulation");

            if (!Storage.connect()) {
                return;
            }

            log.info("Setting up game");

            WalkwaysManager.getInstance();
            ItemManager.getInstance();
            CatalogueManager.getInstance();
            RoomModelManager.getInstance();
            RoomManager.getInstance();
            PlayerManager.getInstance();
            FuserightsManager.getInstance();
            NavigatorManager.getInstance();
            GameScheduler.getInstance();
            CommandManager.getInstance();
            MessageHandler.getInstance();
            TextsManager.getInstance();

            // Get the server variables for the socket to listen on
            serverIP = ServerConfiguration.getString("server.bind");
            serverPort = ServerConfiguration.getInteger("server.port");

            // Get an InetAddressValidator
            InetAddressValidator validator = InetAddressValidator.getInstance();

            // Validate an IPv4 or IPv6 address
            if (!validator.isValid(serverIP)) {
                log.error("%s is not a valid IP", serverIP);
                return;
            }

            // getByName parses IPv6, IPv4 and DNS all in one go
            serverIP = InetAddress.getByName(serverIP).getHostAddress();

            // Create the server instance
            // TODO: listen on both IPv4 and IPv6 if serverIP is 0.0.0.0 or localhost
            server = new NettyServer(serverIP, serverPort);
            server.createSocket();
            server.bind();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> exit()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exit() {
        dispose();
        System.exit(0); // 0 = success
    }

    private static void dispose() {
        log.info("Shutting down server!");
        isShutdown = true;

        // TODO: all the managers
        PlayerManager.getInstance().dispose();

        // Stop listening
        getServer().getWorkerGroup().shutdownGracefully();
        getServer().getBossGroup().shutdownGracefully();
    }

    /**
     * Returns the interface to the server handler
     * 
     * @return {@link NettyServer} interface
     */
    public static NettyServer getServer() {
        return server;
    }

    /**
     * Gets the server IPv4 IP address it is currently (or attempting to) listen on
     * @return IP as string
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Gets the server port it is currently (or attempting to) listen on
     * @return string of IP
     */
    public static int getServerPort() {
        return serverPort;
    }

    /**
     * Gets the startup time.
     *
     * @return the startup time
     */
    public static long getStartupTime() {
        return startupTime;
    }

    public static boolean getIsShutdown() {
        return isShutdown;
    }
}