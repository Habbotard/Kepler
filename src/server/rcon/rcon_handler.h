#ifndef RCON_HANDLER_H
#define RCON_HANDLER_H

#include "uv.h"

void rcon_handle_command(uv_stream_t *handle, int header);
void rcon_send(uv_stream_t *handle, char *data);


#endif