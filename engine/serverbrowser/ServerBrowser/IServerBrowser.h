// IServerBrowser.h - Server browser interface
#ifndef ISERVERBROWSER_H
#define ISERVERBROWSER_H

#include "platform.h"

class IServerBrowser
{
public:
	virtual ~IServerBrowser() {}
	virtual void Open(void) = 0;
	virtual void Close(void) = 0;
	virtual bool IsActiveDialog(void) = 0;
	virtual bool JoinGame(void) = 0;
	virtual void JoinGame(unsigned int serverID) = 0;
	virtual void JoinGame(const char *server, const char *password) = 0;
	virtual void CreateServerGameUI(void) = 0;
	virtual void CloseExistingGameUI(void) = 0;
};

#define SERVERBROWSER_INTERFACE_VERSION "ServerBrowser003"

#endif // ISERVERBROWSER_H
