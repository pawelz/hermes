#ifndef _HERMES_LIBMAILDIR_H
#define _HERMES_LIBMAILDIR_H

#include <stddef.h>

int deliver_to_maildir(const char *maildir_path, const char *email_buffer, size_t email_buffer_size);

#endif