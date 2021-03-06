-- The following script would be used to create a Worker Queue for a Receiver

CREATE TABLE [dbo].[message_inq](
	[recordId] [bigint] IDENTITY(1,1) NOT NULL,
	[messageId] [varchar](255) NULL,
	[payloadName] [varchar](255) NULL,
	[payloadBinaryContent] [image] NULL,
	[payloadTextContent] [text] NULL,
	[localFileName] [varchar](255) NOT NULL,
	[service] [varchar](255) NOT NULL,
	[action] [varchar](255) NOT NULL,
	[arguments] [varchar](255) NULL,
	[fromPartyId] [char](50) NULL,
	[messageRecipient] [varchar](255) NULL,
	[errorCode] [varchar](255) NULL,
	[errorMessage] [varchar](255) NULL,
	[processingStatus] [varchar](255) NULL,
	[applicationStatus] [varchar](255) NULL,
	[encryption] [varchar](10) NOT NULL,
	[receivedTime] [varchar](255) NULL,
	[lastUpdateTime] [varchar](255) NULL,
	[processId] [varchar](255) NULL) 