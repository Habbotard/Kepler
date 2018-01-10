#ifndef INCOMING_MESSAGE_H
#define INCOMING_MESSAGE_H

#include <shared.h>

typedef struct player_s player;
typedef struct dyad_Stream dyad_Stream;

typedef struct {
    char *header;
    char *data;
    int counter;
    int header_id;
} incoming_message;

typedef struct {
    incoming_message *im;
    player *player;
} request;

incoming_message *im_create(char*);
char *im_read_b64(incoming_message*);
char *im_read_str(incoming_message *);
void im_cleanup(incoming_message*);

#endif