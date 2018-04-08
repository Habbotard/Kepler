#include "communication/messages/incoming_message.h"

#include "message_handler.h"
#include <shared.h>

#include "game/player/player.h"

// Login
#include "communication/incoming/login/INIT_CRYPTO.h"
#include "communication/incoming/login/GENERATEKEY.h"
#include "communication/incoming/login/TRY_LOGIN.h"
#include "communication/incoming/login/GDATE.h"

// Register
#include "communication/incoming/register/APPROVENAME.h"
#include "communication/incoming/register/APPROVE_PASSWORD.h"
#include "communication/incoming/register/APPROVEEMAIL.h"
#include "communication/incoming/register/PARENT_EMAIL_REQUIRED.h"
#include "communication/incoming/register/CHECK_AGE.h"
#include "communication/incoming/register/REGISTER.h"

// User
#include "communication/incoming/user/GET_INFO.h"
#include "communication/incoming/user/GET_CREDITS.h"
#include "communication/incoming/user/UPDATE.h"
#include "communication/incoming/user/UPDATE_ACCOUNT.h"

// Messenger
#include "communication/incoming/messenger/MESSENGERINIT.h"
#include "communication/incoming/messenger/FINDUSER.h"
#include "communication/incoming/messenger/MESSENGER_ASSIGNPERSMSG.h"
#include "communication/incoming/messenger/MESSENGER_REQUESTBUDDY.h"
#include "communication/incoming/messenger/MESSENGER_DECLINEBUDDY.h"
#include "communication/incoming/messenger/MESSENGER_ACCEPTBUDDY.h"
#include "communication/incoming/messenger/MESSENGER_REMOVEBUDDY.h"
#include "communication/incoming/messenger/MESSENGER_GETREQUESTS.h"
#include "communication/incoming/messenger/MESSENGER_SENDMSG.h"
#include "communication/incoming/messenger/MESSENGER_GETMESSAGES.h"
#include "communication/incoming/messenger/MESSENGER_MARKREAD.h"

// Navigator
#include "communication/incoming/navigator/NAVIGATE.h"
#include "communication/incoming/navigator/SUSERF.h"
#include "communication/incoming/navigator/GETUSERFLATCATS.h"

// Room
#include "communication/incoming/room/GETINTERST.h"
#include "communication/incoming/room/room_directory.h"
#include "communication/incoming/room/TRYFLAT.h"
#include "communication/incoming/room/GOTOFLAT.h"
#include "communication/incoming/room/GETROOMAD.h"
#include "communication/incoming/room/G_HMAP.h"
#include "communication/incoming/room/G_OBJS.h"
#include "communication/incoming/room/G_ITEMS.h"
#include "communication/incoming/room/G_STAT.h"
#include "communication/incoming/room/G_USRS.h"
#include "communication/incoming/room/GET_FURNI_REVISIONS.h"

// Pool
#include "communication/incoming/room/pool/SWIMSUIT.h"
#include "communication/incoming/room/pool/SPLASH.h"
#include "communication/incoming/room/pool/DIVE.h"

// Room user
#include "communication/incoming/room/user/QUIT.h"
#include "communication/incoming/room/user/WALK.h"
#include "communication/incoming/room/user/CHAT.h"
#include "communication/incoming/room/user/SHOUT.h"
#include "communication/incoming/room/user/WAVE.h"

// Room settings
#include "communication/incoming/room/settings/CREATEFLAT.h"
#include "communication/incoming/room/settings/SETFLATINFO.h"
#include "communication/incoming/room/settings/GETFLATCAT.h"
#include "communication/incoming/room/settings/GETFLATINFO.h"
#include "communication/incoming/room/settings/SETFLATCAT.h"

// Catalogue
#include "communication/incoming/catalogue/GCIX.h"
#include "communication/incoming/catalogue/GCAP.h"


/**
 * Assigns all header handlers to this array
 */
void message_handler_init() {
    // Login
    message_requests[206] = INIT_CRYPTO;
    message_requests[202] = GENERATEKEY;
    message_requests[4] = TRY_LOGIN;
    message_requests[49] = GDATE;

    // Register
    message_requests[42] = APPROVENAME;
    message_requests[203] = APPROVE_PASSWORD;
    message_requests[197] = APPROVEEMAIL;
    message_requests[146] = PARENT_EMAIL_REQUIRED;
    message_requests[46] = CHECK_AGE;
    message_requests[43] = REGISTER;

    // User
    message_requests[7] = GET_INFO;
    message_requests[8] = GET_CREDITS;
    message_requests[44] = UPDATE;
    message_requests[149] = UPDATE_ACCOUNT;

    // Messenger
    message_requests[12] = MESSENGERINIT; 
    message_requests[41] = FINDUSER;
    message_requests[40] = MESSENGER_REMOVEBUDDY;
    message_requests[36] = MESSENGER_ASSIGNPERSMSG;
    message_requests[39] = MESSENGER_REQUESTBUDDY;
    message_requests[38] = MESSENGER_DECLINEBUDDY;
    message_requests[37] = MESSENGER_ACCEPTBUDDY;
    message_requests[233] = MESSENGER_GETREQUESTS;
    message_requests[33] = MESSENGER_SENDMSG;
    message_requests[191] = MESSENGER_GETMESSAGES;
    message_requests[32] = MESSENGER_MARKREAD;
    
    // Navigator
    message_requests[150] = NAVIGATE;
    message_requests[16] = SUSERF;
    message_requests[151] = GETUSERFLATCATS;

    // Room
    message_requests[182] = GETINTERST;
    message_requests[2] = room_directory;
    message_requests[57] = TRYFLAT; // @y1052/123
    message_requests[59] = GOTOFLAT;
    message_requests[126] = GETROOMAD;
    message_requests[60] = G_HMAP;
    message_requests[62] = G_OBJS;
    message_requests[64] = G_STAT;
    message_requests[63] = G_ITEMS;
    message_requests[61] = G_USRS;
    message_requests[213] = GET_FURNI_REVISIONS;

    // Pool
    message_requests[116] = SWIMSUIT;
    message_requests[106] = DIVE;
    message_requests[107] = SPLASH;

    // Room user
    message_requests[53] = QUIT;
    message_requests[75] = WALK;
    message_requests[52] = CHAT;
    message_requests[55] = SHOUT;
    message_requests[94] = WAVE;

    // Room settings
    message_requests[21] = GETFLATINFO;
    message_requests[29] = CREATEFLAT;
    message_requests[25] = SETFLATINFO;
    message_requests[152] = GETFLATCAT;
    message_requests[153] = SETFLATCAT;

    // Catalogue
    message_requests[101] = GCIX;
    message_requests[102] = GCAP;

    /*Client [0.0.0.0] incoming data: 203 / CK@Dalex@F123456
hello!
Client [0.0.0.0] incoming data: 149 / BU@M@C123@H@J07.04.1992@C@F123456*/
}

/**
 * Retrieves the handler by header id
 * @param im the incoming message struct
 * @param player the player struct
 */
void message_handler_invoke(incoming_message *im, player *player) {
    printf("Client [%s] incoming data: %i / %s\n", player->ip_address, im->header_id, im->data);

    if (message_requests[im->header_id] == NULL) {
        return;
    }

    mh_request handle = message_requests[im->header_id];
    handle(player, im);
}