/* -*- Mode: C++; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * The contents of this file are subject to the Netscape Public License
 * Version 1.0 (the "NPL"); you may not use this file except in
 * compliance with the NPL.  You may obtain a copy of the NPL at
 * http://www.mozilla.org/NPL/
 *
 * Software distributed under the NPL is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the NPL
 * for the specific language governing rights and limitations under the
 * NPL.
 *
 * The Initial Developer of this code under the NPL is Netscape
 * Communications Corporation.  Portions created by Netscape are
 * Copyright (C) 1998 Netscape Communications Corporation.  All Rights
 * Reserved.
 */

#include "msgCore.h"    // precompiled header...
#include "nsXPIDLString.h"
#include "nsIPref.h"

#include "nsSmtpService.h"
#include "nsIMsgMailSession.h"
#include "nsIMsgIdentity.h"
#include "nsMsgBaseCID.h"
#include "nsMsgCompCID.h"

#include "nsSmtpUrl.h"
#include "nsSmtpProtocol.h"
#include "nsIFileSpec.h"
#include "nsCOMPtr.h"


typedef struct _findServerByKeyEntry {
    const char *key;
    nsISmtpServer *server;
} findServerByKeyEntry;


static NS_DEFINE_CID(kCSmtpUrlCID, NS_SMTPURL_CID);

// foward declarations...

nsresult
NS_MsgBuildMailtoUrl(nsIFileSpec * aFilePath,
                     const char* aHostName, const char* aSender, 
                     const char * aRecipients, nsIUrlListener *,
                     nsIURI ** aUrl);
nsresult NS_MsgLoadMailtoUrl(nsIURI * aUrl, nsISupports * aConsumer);

nsSmtpService::nsSmtpService()
{
    NS_INIT_REFCNT();
    NS_NewISupportsArray(getter_AddRefs(mSmtpServers));
}

nsSmtpService::~nsSmtpService()
{
    // save the SMTP servers to disk

}

NS_IMPL_THREADSAFE_ADDREF(nsSmtpService);
NS_IMPL_THREADSAFE_RELEASE(nsSmtpService);

nsresult nsSmtpService::QueryInterface(const nsIID &aIID, void** aInstancePtr)
{
    if (NULL == aInstancePtr)
        return NS_ERROR_NULL_POINTER;
    if (aIID.Equals(NS_GET_IID(nsISmtpService)) || aIID.Equals(NS_GET_IID(nsISupports)))
	{
        *aInstancePtr = (void*) ((nsISmtpService*)this);
        NS_ADDREF_THIS();
        return NS_OK;
    }
	if (aIID.Equals(NS_GET_IID(nsIProtocolHandler)))
	{
		*aInstancePtr = (void *) ((nsIProtocolHandler*) this);
		NS_ADDREF_THIS();
		return NS_OK;
	}
    return NS_NOINTERFACE;
}

static NS_DEFINE_CID(kCMsgMailSessionCID, NS_MSGMAILSESSION_CID); 

nsresult nsSmtpService::SendMailMessage(nsIFileSpec * aFilePath,
                                        const char * aRecipients, 
										nsIUrlListener * aUrlListener,
                                        nsISmtpServer * aServer,
                                        nsIURI ** aURL)
{
	nsIURI * urlToRun = nsnull;
	nsresult rv = NS_OK;

	NS_LOCK_INSTANCE();
	// get the current identity from the mail session....
	NS_WITH_SERVICE(nsIMsgMailSession, mailSession, kCMsgMailSessionCID, &rv); 
	if (NS_SUCCEEDED(rv) && mailSession)
	{
		nsCOMPtr<nsIMsgIdentity> identity;
		rv = mailSession->GetCurrentIdentity(getter_AddRefs(identity));

		if (NS_SUCCEEDED(rv) && identity)
		{
            nsXPIDLCString hostName;
            nsXPIDLCString senderName;

			identity->GetSmtpHostname(getter_Copies(hostName));
			identity->GetSmtpUsername(getter_Copies(senderName));

            // todo:  are we leaking hostName and senderName

			rv = NS_MsgBuildMailtoUrl(aFilePath, hostName, senderName, aRecipients, aUrlListener, &urlToRun); // this ref counts urlToRun
			if (NS_SUCCEEDED(rv) && urlToRun)
			{	
				rv = NS_MsgLoadMailtoUrl(urlToRun, nsnull);
			}

			if (aURL) // does the caller want a handle on the url?
				*aURL = urlToRun; // transfer our ref count to the caller....
			else
				NS_IF_RELEASE(urlToRun);

		} // if we have an identity
		else
			NS_ASSERTION(0, "no current identity found for this user....");
	} // if we had a mail session

	NS_UNLOCK_INSTANCE();
	return rv;
}


// The following are two convience functions I'm using to help expedite building and running a mail to url...

// short cut function for creating a mailto url...
nsresult NS_MsgBuildMailtoUrl(nsIFileSpec * aFilePath,
                              const char* aHostName, const char* aSender, 
							  const char * aRecipients, nsIUrlListener * aUrlListener, nsIURI ** aUrl)
{
	// mscott: this function is a convience hack until netlib actually dispatches smtp urls.
	// in addition until we have a session to get a password, host and other stuff from, we need to use default values....
	// ..for testing purposes....
	
	nsresult rv = NS_OK;
	nsCOMPtr <nsISmtpUrl> smtpUrl;
	rv = nsComponentManager::CreateInstance(kCSmtpUrlCID, NULL, nsCOMTypeInfo<nsISmtpUrl>::GetIID(), getter_AddRefs(smtpUrl));

	if (NS_SUCCEEDED(rv) && smtpUrl)
	{
		char * urlSpec= PR_smprintf("mailto://%s:%d/%s",
                                    (const char*)aHostName, 25, aRecipients ? aRecipients : "");
		if (urlSpec)
		{
			nsCOMPtr<nsIMsgMailNewsUrl> url = do_QueryInterface(smtpUrl);
			url->SetSpec(urlSpec);
			smtpUrl->SetPostMessageFile(aFilePath);
			// this cast is safe....it is necessary because of a bug to the xpidl compiler
			smtpUrl->SetUserEmailAddress((char *) aSender);
			url->RegisterListener(aUrlListener);
			PR_Free(urlSpec);
		}
		rv = smtpUrl->QueryInterface(nsCOMTypeInfo<nsIURI>::GetIID(), (void **) aUrl);
	 }

	 return rv;
}

nsresult NS_MsgLoadMailtoUrl(nsIURI * aUrl, nsISupports * aConsumer)
{
	// mscott: this function is pretty clumsy right now...eventually all of the dispatching
	// and transport creation code will live in netlib..this whole function is just a hack
	// for our mail news demo....

	// for now, assume the url is a news url and load it....
	nsCOMPtr <nsISmtpUrl> smtpUrl;
	nsSmtpProtocol	*smtpProtocol = nsnull;
	nsresult rv = NS_OK;

	if (!aUrl)
		return rv;

    // turn the url into an smtp url...
	smtpUrl = do_QueryInterface(aUrl);
    if (smtpUrl)
    {
		// almost there...now create a nntp protocol instance to run the url in...
		smtpProtocol = new nsSmtpProtocol(aUrl);
		if (smtpProtocol == nsnull)
			rv = NS_ERROR_OUT_OF_MEMORY;
		else
			smtpProtocol->LoadUrl(aUrl); // protocol will get destroyed when url is completed...
	}

	return rv;
}

NS_IMETHODIMP nsSmtpService::GetScheme(char * *aScheme)
{
	nsresult rv = NS_OK;
	if (aScheme)
		*aScheme = PL_strdup("mailto");
	else
		rv = NS_ERROR_NULL_POINTER;
	return rv; 
}

NS_IMETHODIMP nsSmtpService::GetDefaultPort(PRInt32 *aDefaultPort)
{
	nsresult rv = NS_OK;
	if (aDefaultPort)
		*aDefaultPort = SMTP_PORT;
	else
		rv = NS_ERROR_NULL_POINTER;
	return rv; 	
}

NS_IMETHODIMP nsSmtpService::MakeAbsolute(const char *aRelativeSpec, nsIURI *aBaseURI, char **_retval)
{
	// no such thing as relative urls for smtp.....
	return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP nsSmtpService::NewURI(const char *aSpec, nsIURI *aBaseURI, nsIURI **_retval)
{
	// i just haven't implemented this yet...I will be though....
	return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP nsSmtpService::NewChannel(const char *verb, nsIURI *aURI, nsILoadGroup *aGroup, nsIEventSinkGetter *eventSinkGetter, nsIChannel **_retval)
{
	// mscott - right now, I don't like the idea of returning channels to the caller. They just want us
	// to run the url, they don't want a channel back...I'm going to be addressing this issue with
	// the necko team in more detail later on.
	return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP
nsSmtpService::GetSmtpServers(nsISupportsArray ** aResult)
{
  NS_ENSURE_ARG_POINTER(aResult);

  nsresult rv;
  
  // now read in the servers from prefs if necessary
  PRUint32 serverCount;
  rv = mSmtpServers->Count(&serverCount);
  if (NS_FAILED(rv)) return rv;

  if (serverCount<=0) loadSmtpServers();

  *aResult = mSmtpServers;
  NS_ADDREF(*aResult);

  return NS_OK;
}

nsresult
nsSmtpService::loadSmtpServers()
{

    nsresult rv;
    NS_WITH_SERVICE(nsIPref, prefs, "component://netscape/preferences", &rv);
    if (NS_FAILED(rv)) return rv;
    
    nsXPIDLCString serverList;
    rv = prefs->CopyCharPref("mail.smtpservers", getter_Copies(serverList));
    if (NS_FAILED(rv)) return rv;

    char *newStr;
    char *pref = nsCRT::strtok((char*)(const char*)serverList, ", ", &newStr);
    while (pref) {
        nsCOMPtr<nsISmtpServer> smtpServer;
        rv = nsComponentManager::CreateInstance(NS_SMTPSERVER_PROGID,
                                                nsnull,
                                                NS_GET_IID(nsISmtpServer),
                                                (void **)getter_AddRefs(smtpServer));
        if (NS_SUCCEEDED(rv)) {
            smtpServer->SetKey(pref);
            mSmtpServers->AppendElement(smtpServer);
        }
        
        pref = nsCRT::strtok(newStr, ", ", &newStr);
    }

    return NS_OK;
}

NS_IMETHODIMP
nsSmtpService::GetDefaultSmtpServer(nsISmtpServer **aServer)
{
  NS_ENSURE_ARG_POINTER(aServer);

  nsresult rv;

  *aServer = nsnull;
  // always returns NS_OK, just leaving *aServer at nsnull
  if (!mDefaultSmtpServer) {
    nsCOMPtr<nsIEnumerator> enumerator;
    rv = mSmtpServers->Enumerate(getter_AddRefs(enumerator));
    if (NS_FAILED(rv)) return NS_OK;
    
    rv = enumerator->First();
    if (NS_FAILED(rv)) return NS_OK;

    nsCOMPtr<nsISupports> curItem;
    rv = enumerator->CurrentItem(getter_AddRefs(curItem));
    if (NS_FAILED(rv)) return NS_OK;
    
    mDefaultSmtpServer = do_QueryInterface(curItem);
  }

  // XXX still need to make sure the default SMTP server is
  // in the server list!
    
  *aServer = mDefaultSmtpServer;
  NS_IF_ADDREF(*aServer);

  return NS_OK;
}

NS_IMETHODIMP
nsSmtpService::SetDefaultSmtpServer(nsISmtpServer *aServer)
{
  // XXX need to make sure the default SMTP server is in the array
  mDefaultSmtpServer = aServer;
  return NS_OK;
}

PRBool
nsSmtpService::findServerByKey (nsISupports *element, void *aData)
{
    nsresult rv;
    nsCOMPtr<nsISmtpServer> server = do_QueryInterface(element, &rv);
    if (NS_FAILED(rv)) return PR_TRUE;
    
    findServerByKeyEntry *entry = (findServerByKeyEntry*) aData;

    nsXPIDLCString key;
    rv = server->GetKey(getter_Copies(key));
    if (NS_FAILED(rv)) return PR_TRUE;

    if (nsCRT::strcmp(key, entry->key)==0) {
        entry->server = server;
        return PR_FALSE;
    }
    
    return PR_TRUE;
}


NS_IMETHODIMP
nsSmtpService::CreateSmtpServer(nsISmtpServer **aResult)
{
    if (!aResult) return NS_ERROR_NULL_POINTER;

    nsresult rv;
    
    PRInt32 i=1;
    PRBool unique = PR_FALSE;

    findServerByKeyEntry entry;
    nsCAutoString key;
    
    do {
        key = "server";
        key.Append(i);
        
        entry.key = key;
        entry.server = nsnull;
        
        mSmtpServers->EnumerateForwards(findServerByKey, (void *)&entry);
        if (!entry.server) unique=PR_TRUE;
        
    } while (!unique);

    rv = nsComponentManager::CreateInstance(NS_SMTPSERVER_PROGID,
                                            nsnull,
                                            NS_GET_IID(nsISmtpServer),
                                            (void **)aResult);
    if (NS_SUCCEEDED(rv))
        (*aResult)->SetKey(key);
            
    return rv;
}

NS_IMETHODIMP
nsSmtpService::DeleteSmtpServer(nsISmtpServer *aServer)
{
    if (!aServer) return NS_OK;

    nsresult rv;

    PRInt32 idx = 0;
    rv = mSmtpServers->GetIndexOf(aServer, &idx);
    if (NS_FAILED(rv) || idx==0)
        return NS_OK;

    rv = mSmtpServers->DeleteElementAt(idx);

    return rv;

}
