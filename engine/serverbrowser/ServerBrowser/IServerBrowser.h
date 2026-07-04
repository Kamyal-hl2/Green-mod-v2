// IServerBrowser.h - Server browser interface
#ifndef ISERVERBROWSER_H
#define ISERVERBROWSER_H

class IServerBrowser
{
public:
	virtual ~IServerBrowser() {}
	virtual const char *GetMapFriendlyNameAndGameType(const char *pszMapName, char *szFriendlyMapName, int cchFriendlyName) = 0;
	virtual void SetWorkshopEnabled(bool bManaged) = 0;
	virtual void AddWorkshopSubscribedMap(const char *pszMapName) = 0;
	virtual void RemoveWorkshopSubscribedMap(const char *pszMapName) = 0;
};

#define SERVERBROWSER_INTERFACE_VERSION "ServerBrowser003"

#endif // ISERVERBROWSER_H
