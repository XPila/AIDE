/**********************************************************************************************************************\
** XBusComm.cpp																										  **
\**********************************************************************************************************************/



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Includes

// Precompiled header
#include <StdAfx.h>

// XBus header
#include "..\..\Header Files\Hw\XBusComm.h"


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Definitions
#define TIMESYNC

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Static functions

void SwapW(void* pData)
{
	unsigned short usData = *((unsigned short*)pData);
	((unsigned char*)pData)[0] = ((unsigned char*)&usData)[1];
	((unsigned char*)pData)[1] = ((unsigned char*)&usData)[0];
}

void SwapD(void* pData)
{
	unsigned long ulData = *((unsigned long*)pData);
	((unsigned char*)pData)[0] = ((unsigned char*)&ulData)[3];
	((unsigned char*)pData)[1] = ((unsigned char*)&ulData)[2];
	((unsigned char*)pData)[2] = ((unsigned char*)&ulData)[1];
	((unsigned char*)pData)[3] = ((unsigned char*)&ulData)[0];
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Classes

CXBusCommPort::CXBusCommPort()
{
	m_bOpened = false;
	m_lBaudRate = 9600;
	m_lPortIndex = 0;
	m_eMode = eXBusMode_Master;
	m_ucNodeID = 0;
	m_eLineType = eXBusLineType_TTL2Wire;
	m_bFilterStartChars = false;
	m_lTxStartChars = 1;
	m_lRxStartChars = 1;
	m_bAck = true;
	m_bLoopBack = true;
	m_lLoopBackTimeOut = 100;
	m_lStartTimeOut = 100;
	m_lHeaderTimeOut = 100;
	m_lDataTimeOut = 100;
	m_lAckTimeOut = 100;
	m_lStartInterleave = 0;
	m_lRxTxChangeInterleave = 0;
	m_lTxRxChangeInterleave = 0;
	m_cStartChar = (char)0x5a;
	m_cAckChar = (char)0xa5;
	m_eError = eXBusError_None;
	m_eDirection = eXBusDirection_Rx;
}

CXBusCommPort::~CXBusCommPort()
{
	if (m_bOpened)
		Close();
}

long CXBusCommPort::GetPortCount()
{
	return m_Comms.GetCount();
}

_bstr_t CXBusCommPort::GetPortName(long lPortIndex)
{
	long lCount = m_Comms.GetCount();
	if ((lPortIndex < 0) || (lPortIndex >= lCount))
		return _bstr_t();
	return m_Comms.GetItem(lPortIndex)->GetName();
}

long CXBusCommPort::GetPortIndex()
{
	return m_lPortIndex;
}

void CXBusCommPort::SetPortIndex(long lPortIndex)
{
	m_lPortIndex = lPortIndex;
}

bool CXBusCommPort::GetOpened()
{
	return m_bOpened;
}

void CXBusCommPort::SetOpened(bool bOpened)
{
	m_bOpened = bOpened;
}

long CXBusCommPort::GetBaudRate()
{
	return m_lBaudRate;
}

void CXBusCommPort::SetBaudRate(long lBaudRate)
{
	m_lBaudRate = lBaudRate;
	if (m_bOpened)
	{
		m_NewState.SetBaudRate(m_lBaudRate);
		m_pComm->SetState(m_NewState);
	}
}

EXBusMode CXBusCommPort::GetMode()
{
	return m_eMode;
}

void CXBusCommPort::SetMode(EXBusMode eMode)
{
	m_eMode = eMode;
}

unsigned char CXBusCommPort::GetNodeID()
{
	return m_ucNodeID;
}

void CXBusCommPort::SetNodeID(unsigned char ucNodeID)
{
	m_ucNodeID = ucNodeID;
}

EXBusLineType CXBusCommPort::GetLineType()
{
	return m_eLineType;
}

void CXBusCommPort::SetLineType(EXBusLineType eLineType, bool bUpdateParams)
{
	m_eLineType = eLineType;
	if (bUpdateParams)
	{
		switch (m_eLineType)
		{
//		case eXBusLineType_None:
		case eXBusLineType_TTL2Wire:
			m_bFilterStartChars = false;
			m_lTxStartChars = 1;
			m_lRxStartChars = 1;
			m_bAck = true;
			m_bLoopBack = true;
			m_lLoopBackTimeOut = 100;
			m_lStartTimeOut = 100;
			m_lHeaderTimeOut = 100;
			m_lDataTimeOut = 100;
			m_lAckTimeOut = 100;
			m_lStartInterleave = 0;
			m_lRxTxChangeInterleave = 0;
			m_lTxRxChangeInterleave = 0;
			m_cStartChar = (char)0x5a;
			m_cAckChar = (char)0xa5;
			break;
		case eXBusLineType_TTL3Wire:
			m_bFilterStartChars = false;
			m_lTxStartChars = 1;
			m_lRxStartChars = 1;
			m_bAck = true;
			m_bLoopBack = false;
			m_lLoopBackTimeOut = 0;
			m_lStartTimeOut = 100;
			m_lHeaderTimeOut = 100;
			m_lDataTimeOut = 100;
			m_lAckTimeOut = 100;
			m_lStartInterleave = 0;
			m_lRxTxChangeInterleave = 0;
			m_lTxRxChangeInterleave = 0;
			m_cStartChar = (char)0x5a;
			m_cAckChar = (char)0xa5;
			break;
		case eXBusLineType_RS232:
			m_bFilterStartChars = false;
			m_lTxStartChars = 1;
			m_lRxStartChars = 1;
			m_bAck = true;
			m_bLoopBack = false;
			m_lLoopBackTimeOut = 0;
			m_lStartTimeOut = 100;
			m_lHeaderTimeOut = 100;
			m_lDataTimeOut = 100;
			m_lAckTimeOut = 100;
			m_lStartInterleave = 0;
			m_lRxTxChangeInterleave = 0;
			m_lTxRxChangeInterleave = 0;
			m_cStartChar = (char)0x5a;
			m_cAckChar = (char)0xa5;
			break;
		case eXBusLineType_RS485:
			m_bFilterStartChars = false;
			m_lTxStartChars = 1;
			m_lRxStartChars = 1;
			m_bAck = true;
			m_bLoopBack = false;
			m_lLoopBackTimeOut = 0;
			m_lStartTimeOut = 100;
			m_lHeaderTimeOut = 100;
			m_lDataTimeOut = 100;
			m_lAckTimeOut = 100;
			m_lStartInterleave = 0;
			m_lRxTxChangeInterleave = 0;
			m_lTxRxChangeInterleave = 0;
			m_cStartChar = (char)0x5a;
			m_cAckChar = (char)0xa5;
			break;
		case eXBusLineType_RF433:
			m_bFilterStartChars = true;
			m_lTxStartChars = 2;
			m_lRxStartChars = 1;
			m_bAck = false;
			m_bLoopBack = false;
			m_lLoopBackTimeOut = 0;
			m_lStartTimeOut = 100;
			m_lHeaderTimeOut = 100;
			m_lDataTimeOut = 100;
			m_lAckTimeOut = 100;
			m_lStartInterleave = 0;
			m_lRxTxChangeInterleave = 2;
			m_lTxRxChangeInterleave = 1;
			m_cStartChar = (char)0x5a;
			m_cAckChar = (char)0xa5;
			break;
		}
	}
}

bool CXBusCommPort::GetAck()
{
	return m_bAck;
}

void CXBusCommPort::SetAck(bool bAck)
{
	m_bAck = bAck;
}

bool CXBusCommPort::GetLoopBack()
{
	return m_bLoopBack;
}

void CXBusCommPort::SetLoopBack(bool bLoopBack)
{
	m_bLoopBack = bLoopBack;
}

long CXBusCommPort::GetLoopBackTimeOut()
{
	return m_lLoopBackTimeOut;
}

void CXBusCommPort::SetLoopBackTimeOut(long lLoopBackTimeOut)
{
	m_lLoopBackTimeOut = lLoopBackTimeOut;
}

long CXBusCommPort::GetStartTimeOut()
{
	return m_lStartTimeOut;
}

void CXBusCommPort::SetStartTimeOut(long lStartTimeOut)
{
	m_lStartTimeOut = lStartTimeOut;
}

long CXBusCommPort::GetHeaderTimeOut()
{
	return m_lHeaderTimeOut;
}

void CXBusCommPort::SetHeaderTimeOut(long lHeaderTimeOut)
{
	m_lHeaderTimeOut = lHeaderTimeOut;
}

long CXBusCommPort::GetDataTimeOut()
{
	return m_lDataTimeOut;
}

void CXBusCommPort::SetDataTimeOut(long lDataTimeOut)
{
	m_lDataTimeOut = lDataTimeOut;
}

long CXBusCommPort::GetAckTimeOut()
{
	return m_lAckTimeOut;
}

void CXBusCommPort::SetAckTimeOut(long lAckTimeOut)
{
	m_lAckTimeOut = lAckTimeOut;
}

long CXBusCommPort::GetStartInterleave()
{
	return m_lStartInterleave;
}

void CXBusCommPort::SetStartInterleave(long lStartInterleave)
{
	m_lStartInterleave = lStartInterleave;
}

long CXBusCommPort::GetRxTxChangeInterleave()
{
	return m_lRxTxChangeInterleave;
}

void CXBusCommPort::SetRxTxChangeInterleave(long lRxTxChangeInterleave)
{
	m_lRxTxChangeInterleave = lRxTxChangeInterleave;
}

long CXBusCommPort::GetTxRxChangeInterleave()
{
	return m_lTxRxChangeInterleave;
}

void CXBusCommPort::SetTxRxChangeInterleave(long lTxRxChangeInterleave)
{
	m_lTxRxChangeInterleave = lTxRxChangeInterleave;
}

char CXBusCommPort::GetStartChar()
{
	return m_cStartChar;
}

void CXBusCommPort::SetStartChar(char cStartChar)
{
	m_cStartChar = cStartChar;
}

char CXBusCommPort::GetAckChar()
{
	return m_cAckChar;
}

void CXBusCommPort::SetAckChar(char cAckChar)
{
	m_cAckChar = cAckChar;
}

EXBusError CXBusCommPort::GetError()
{
	return m_eError;
}

EXBusDirection CXBusCommPort::GetDirection()
{
	return m_eDirection;
}

bool CXBusCommPort::Open(long lPortIndex)
{
	if (!m_bOpened)
	{
		if (lPortIndex != -1)
			m_lPortIndex = lPortIndex;
		m_pComm = m_Comms.GetItem(m_lPortIndex);
		if (m_pComm == NULL) return false;
		m_pComm->Open(false);
		m_OldState = m_pComm->GetState();
		m_OldTimeOuts = m_pComm->GetTimeOuts();
		m_NewState.Build("9600, n, 8, 1");
		m_NewState.SetBaudRate(m_lBaudRate);
		m_NewState.SetDTRControl(eCommDTRControl_Enable);
		m_NewState.SetRTSControl(eCommRTSControl_Enable);
		m_NewState.SetBinaryMode(true);
		m_NewState.SetTXContinueOnXOff(true);
		m_pComm->SetState(m_NewState);
		m_NewTimeOuts.SetTimeOutReadInterval(0);
		m_NewTimeOuts.SetTimeOutReadMultiplier(0);
		m_NewTimeOuts.SetTimeOutReadConstant(0);
		m_NewTimeOuts.SetTimeOutWriteMultiplier(0);
		m_NewTimeOuts.SetTimeOutWriteConstant(0);
		m_pComm->SetTimeOuts(m_NewTimeOuts);
//		m_pComm->SetDTR();
		if (m_eLineType == eXBusLineType_RF433)
			m_pComm->ClrRTS();
		else
//			m_pComm->SetRTS();
		m_pComm->Purge((ECommPurgeFlags)(eCommPurgeFlags_TXAbort | eCommPurgeFlags_RXAbort | eCommPurgeFlags_TXClear | eCommPurgeFlags_RXClear));
		m_eError = eXBusError_None;
		m_eDirection = eXBusDirection_Rx;
		m_bOpened = true;
		m_dCharTime_ms = (double)10000 / m_lBaudRate;
	}
	return true;
}

void CXBusCommPort::Close()
{
	if (m_bOpened)
	{
		m_pComm->SetState(m_OldState);
		m_pComm->SetTimeOuts(m_OldTimeOuts);
		m_pComm->Close();
		m_bOpened = false;
	}
}

bool CXBusCommPort::ReceiveMessage(SXBusMessage* psMessage)
{
	if (ReceiveStart())
		if (ReceiveMessageHeader(psMessage))
			if (ReceiveMessageData(psMessage))
				switch (m_eMode)
				{
				case eXBusMode_None:
					if (ReceiveAck())
					{
						SWAP_W(&psMessage->sHeader.usMessageID)
						return true;
					}
					break;
				case eXBusMode_Master:
					if (SendAck())
					{
						SWAP_W(&psMessage->sHeader.usMessageID)
						return true;
					}
					break;
				case eXBusMode_Slave:
					if (psMessage->sHeader.ucNodeID == m_ucNodeID)
					{
						if (SendAck())
						{
							SWAP_W(&psMessage->sHeader.usMessageID)
							return true;
						}
					}
					else
						return true;
					break;
				}
	return false;
}

bool CXBusCommPort::SendMessage(SXBusMessage* psMessage)
{
	SWAP_W(&psMessage->sHeader.usMessageID)
	if (SendStart())
		if (SendMessageHeader(psMessage))
			if (SendMessageData(psMessage))
				if (ReceiveAck())
				{
					SetDirection(eXBusDirection_Rx);
					return true;
				}
	SetDirection(eXBusDirection_Rx);
	return false;
}

bool CXBusCommPort::ReceiveMessage(unsigned char* pucNodeID, unsigned short* pusMessageID, bool* pbRead, bool* pbWrite, unsigned char* pucDataSize, unsigned char* pucData)
{
	SXBusMessage sMessage;
	if (ReceiveMessage(&sMessage))
	{
		if (pucNodeID != NULL)
			*pucNodeID = sMessage.sHeader.ucNodeID;
		if (pusMessageID != NULL)
			*pusMessageID = sMessage.sHeader.usMessageID;
		if (pbRead != NULL)
			*pbRead = sMessage.sHeader.ucRead;
		if (pbWrite != NULL)
			*pbWrite = sMessage.sHeader.ucWrite;
		if (sMessage.sHeader.ucData)
		{
			if (pucDataSize != NULL)
				*pucDataSize = sMessage.sHeader.ucDataSize + 1;
			if ((pucData != NULL) && (!sMessage.sHeader.ucRead))
				memcpy(pucData, sMessage.ucData, sMessage.sHeader.ucDataSize + 1);
		}
		else
			if (pucDataSize != NULL)
				*pucDataSize = 0;
		SetDirection(eXBusDirection_Rx);
		return true;
	}
	SetDirection(eXBusDirection_Rx);
	return false;
}

bool CXBusCommPort::SendMessage(unsigned char ucNodeID, unsigned short usMessageID, bool bRead, bool bWrite, unsigned char ucDataSize, unsigned char* pucData)
{
	SXBusMessage sMessage;
	sMessage.sHeader.ucNodeID = ucNodeID;
	sMessage.sHeader.usMessageID = usMessageID;
	if (ucDataSize > 0)
		sMessage.sHeader.ucDataSize = (ucDataSize - 1);
	else
		sMessage.sHeader.ucDataSize = 0;
	sMessage.sHeader.ucData = (ucDataSize > 0);
	sMessage.sHeader.ucReserved = 0;
	sMessage.sHeader.ucRead = bRead;
	sMessage.sHeader.ucWrite = bWrite;
	if (sMessage.sHeader.ucData)
		if ((pucData != NULL) && (!sMessage.sHeader.ucRead))
			memcpy(sMessage.ucData, pucData, ucDataSize);
	return SendMessage(&sMessage);
}

bool CXBusCommPort::Command(unsigned char ucNodeID, unsigned short usMessageID, unsigned char ucDataSize, unsigned char* pucData)
{
	SXBusMessage sMessage;
	sMessage.sHeader.ucNodeID = ucNodeID;
	sMessage.sHeader.usMessageID = usMessageID;
	if (ucDataSize > 0)
		sMessage.sHeader.ucDataSize = (ucDataSize - 1);
	else
		sMessage.sHeader.ucDataSize = 0;
	sMessage.sHeader.ucData = (ucDataSize > 0);
	sMessage.sHeader.ucReserved = 0;
	sMessage.sHeader.ucRead = 0;
	sMessage.sHeader.ucWrite = 0;
	if (sMessage.sHeader.ucData)
	{
		if (pucData != NULL)
			memcpy(sMessage.ucData, pucData, ucDataSize);
		else
			memset(sMessage.ucData, 0, ucDataSize);
	}
	if (SendMessage(&sMessage))
		return true;
	return false;
}

bool CXBusCommPort::ReadObject(unsigned char ucNodeID, unsigned short usMessageID, unsigned char ucDataSize, unsigned char* pucData)
{
	SXBusMessage sMessage;
	sMessage.sHeader.ucNodeID = ucNodeID;
	sMessage.sHeader.usMessageID = usMessageID;
	if (ucDataSize > 0)
		sMessage.sHeader.ucDataSize = (ucDataSize - 1);
	else
		sMessage.sHeader.ucDataSize = 0;
	sMessage.sHeader.ucData = (ucDataSize > 0);
	sMessage.sHeader.ucReserved = 0;
	sMessage.sHeader.ucRead = 1;
	sMessage.sHeader.ucWrite = 0;
	if (SendMessage(&sMessage))
		if (ReceiveMessage(&sMessage))
		{
			if (pucData != NULL)
				if (sMessage.sHeader.ucData)
					memcpy(pucData, sMessage.ucData, sMessage.sHeader.ucDataSize + 1);
			return true;
		}
	return false;
}

bool CXBusCommPort::WriteObject(unsigned char ucNodeID, unsigned short usMessageID, unsigned char ucDataSize, unsigned char* pucData)
{
	SXBusMessage sMessage;
	sMessage.sHeader.ucNodeID = ucNodeID;
	sMessage.sHeader.usMessageID = usMessageID;
	if (ucDataSize > 0)
		sMessage.sHeader.ucDataSize = (ucDataSize - 1);
	else
		sMessage.sHeader.ucDataSize = 0;
	sMessage.sHeader.ucData = (ucDataSize > 0);
	sMessage.sHeader.ucReserved = 0;
	sMessage.sHeader.ucRead = 0;
	sMessage.sHeader.ucWrite = 1;
	if (sMessage.sHeader.ucData)
	{
		if (pucData != NULL)
			memcpy(sMessage.ucData, pucData, ucDataSize);
		else
			memset(sMessage.ucData, 0, ucDataSize);
	}
	if (SendMessage(&sMessage))
		return true;
	return false;
}

char* CXBusCommPort::GetErrorText()
{
	switch (m_eError)
	{
	case eXBusError_None:		return "None";
	case eXBusError_TimeOut:	return "TimeOut";
	case eXBusError_CheckSum:	return "CheckSum";
	case eXBusError_LoopBack:	return "LoopBack";
	}
	return NULL;
}

unsigned char CXBusCommPort::CheckSum(unsigned char* pucData, unsigned long ulCount, unsigned char ucCheckSum)
{
	for (unsigned long ulIndex = 0; ulIndex < ulCount; ulIndex ++)
		ucCheckSum ^= pucData[ulIndex];
	return ucCheckSum;
}

void CXBusCommPort::SetDirection(EXBusDirection eDirection)
{
	if (m_eDirection != eDirection)
	{
		m_eDirection = eDirection;
		if (m_bOpened)
		{
			if (m_eDirection == eXBusDirection_Rx)
			{
				m_pComm->Purge((ECommPurgeFlags)(eCommPurgeFlags_RXAbort | eCommPurgeFlags_RXClear));
				if (m_eLineType == eXBusLineType_RF433)
				{
					m_pComm->ClrRTS();
					if (m_lTxRxChangeInterleave > 0)
						Sleep(m_lTxRxChangeInterleave);
				}
			}
			if (m_eDirection == eXBusDirection_Tx)
				if (m_eLineType == eXBusLineType_RF433)
				{
					m_pComm->SetRTS();
					if (m_lRxTxChangeInterleave > 0)
						Sleep(m_lRxTxChangeInterleave);
				}
		}
	}
}

bool CXBusCommPort::ReceiveChar(char* pcChar, long lTimeOut)
{
	m_eError = eXBusError_None;
	char cChar;
	unsigned long ulReaded = 0;
	ERR_DEF
	ENTER
	SetDirection(eXBusDirection_Rx);
	ulReaded = m_pComm->Read(&cChar, 1, lTimeOut);
	LEAVE
	if ((ERR != S_OK) || (ulReaded < 1))
	{
		m_eError = eXBusError_TimeOut;
		return false;
	}
	if (pcChar != NULL)
		*pcChar = cChar;
	return true;
}

bool CXBusCommPort::SendChar(char cChar, long lTimeOut)
{
	m_eError = eXBusError_None;
	SetDirection(eXBusDirection_Tx);
	m_pComm->Write(&cChar, 1);
	if (m_bLoopBack)
	{
		char cEchoChar;
		unsigned long ulReaded = 0;
		ERR_DEF
		ENTER
		ulReaded = m_pComm->Read(&cEchoChar, 1, lTimeOut);
		LEAVE
		if ((ERR != S_OK) || (ulReaded < 1))
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
		if (cEchoChar != cChar)
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
	}
	return true;
}

bool CXBusCommPort::ReceiveChars(char* pcChars, long lCount, long lTimeOut)
{
	m_eError = eXBusError_None;
	if (lCount > MAX_CHAR_COUNT)
		return false;
	char cChars[MAX_CHAR_COUNT];
	unsigned long ulReaded = 0;
	ERR_DEF
	ENTER
	SetDirection(eXBusDirection_Rx);
	ulReaded = m_pComm->Read(cChars, lCount, lTimeOut);
	LEAVE
	if ((ERR != S_OK) || ((signed)ulReaded < lCount))
	{
		m_eError = eXBusError_TimeOut;
		return false;
	}
	if (pcChars != NULL)
		memcpy(pcChars, cChars, lCount);
	return true;
}

bool CXBusCommPort::SendChars(char* pcChars, long lCount, long lTimeOut)
{
	m_eError = eXBusError_None;
	if (lCount > MAX_CHAR_COUNT)
		return false;
	if (pcChars == NULL)
		return false;
	SetDirection(eXBusDirection_Tx);
	m_pComm->Write(&pcChars, lCount);
	if (m_bLoopBack)
	{
		char cEchoChars[MAX_CHAR_COUNT];
		unsigned long ulReaded = 0;
		ERR_DEF
		ENTER
		ulReaded = m_pComm->Read(cEchoChars, lCount, lTimeOut);
		LEAVE
		if ((ERR != S_OK) || ((signed)ulReaded < lCount))
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
		if (memcmp(cEchoChars, pcChars, lCount) != 0)
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
	}
	return true;
}

bool CXBusCommPort::ReceiveData(unsigned char* pucData, long lCount, long lTimeOut)
{
	m_eError = eXBusError_None;
	if (lCount > MAX_DATA_COUNT)
		return false;
	unsigned char ucData[MAX_DATA_COUNT];
	unsigned long ulReaded = 0;
	ERR_DEF
	ENTER
	SetDirection(eXBusDirection_Rx);
	ulReaded = m_pComm->Read(ucData, lCount, lTimeOut);
	LEAVE
	if ((ERR != S_OK) || ((signed)ulReaded < lCount))
	{
		m_eError = eXBusError_TimeOut;
		return false;
	}
	unsigned char ucCheckSum;
	ulReaded = 0;
	ENTER
	ulReaded = m_pComm->Read(&ucCheckSum, 1, lTimeOut);
	LEAVE
	if ((ERR != S_OK) || (ulReaded < 1))
	{
		m_eError = eXBusError_TimeOut;
		return false;
	}
	if (ucCheckSum != CheckSum(ucData, lCount))
	{
		m_eError = eXBusError_CheckSum;
		return false;
	}
	if (pucData != NULL)
		memcpy(pucData, ucData, lCount);
	return true;
}

bool CXBusCommPort::SendData(unsigned char* pucData, long lCount, long lTimeOut)
{
	m_eError = eXBusError_None;
	if (lCount > MAX_DATA_COUNT)
		return false;
	if (pucData == NULL)
		return false;
	SetDirection(eXBusDirection_Tx);
	m_pComm->Write(pucData, lCount);
	if (m_bLoopBack)
	{
		unsigned char ucEchoData[MAX_DATA_COUNT];
		unsigned long ulReaded = 0;
		ERR_DEF
		ENTER
		ulReaded = m_pComm->Read(ucEchoData, lCount, lTimeOut);
		LEAVE
		if ((ERR != S_OK) || ((signed)ulReaded < lCount))
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
		if (memcmp(ucEchoData, pucData, lCount) != 0)
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
	}
	unsigned char ucCheckSum = CheckSum(pucData, lCount);
	m_pComm->Write(&ucCheckSum, 1);
	if (m_bLoopBack)
	{
		unsigned char ucCheckSumEcho;
		unsigned long ulReaded = 0;
		ERR_DEF
		ENTER
		ulReaded = m_pComm->Read(&ucCheckSumEcho, 1, lTimeOut);
		LEAVE
		if ((ERR != S_OK) || (ulReaded < 1))
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
		if (ucCheckSumEcho != ucCheckSum)
		{
			m_eError = eXBusError_LoopBack;
			return false;
		}
	}
	return true;
}

bool CXBusCommPort::ReceiveStart()
{
	char cStartChar;
	if (m_bFilterStartChars)
	{
		unsigned long ulStartTime = GetTickCount();
		while ((ulStartTime + m_lStartTimeOut) > GetTickCount())
		{
			if (!ReceiveChar(&cStartChar, m_lStartTimeOut - (GetTickCount() - ulStartTime)))
				return false;
			if (cStartChar == m_cStartChar)
				break;
		}
		long lStartChars = 1;
		while ((ulStartTime + m_lStartTimeOut) > GetTickCount())
		{
			if (!ReceiveChar(&cStartChar, m_lStartTimeOut - (GetTickCount() - ulStartTime)))
				return false;
			if (cStartChar == m_cStartChar)
				lStartChars ++;
			else
				if (lStartChars >= m_lRxStartChars)
				{
					m_ucNoStartByte = (unsigned)cStartChar;
					return true;
				}
				else
					return false;
		}
		return false;
	}
	else
	{
		if (m_lRxStartChars > 1)
		{
			for (long lIndex = 0; lIndex < m_lRxStartChars; lIndex ++)
			{
				if (!ReceiveChar(&cStartChar, m_lStartTimeOut))
					return false;
				if (cStartChar != m_cStartChar)
					return false;
			}
			return true;
		}
		else
			if (ReceiveChar(&cStartChar, m_lStartTimeOut))
				if (cStartChar == m_cStartChar)
					return true;
	}
	return false;
}

bool CXBusCommPort::SendStart()
{
	m_pComm->Purge((ECommPurgeFlags)(eCommPurgeFlags_TXAbort | eCommPurgeFlags_RXAbort | eCommPurgeFlags_TXClear | eCommPurgeFlags_RXClear));
	if (m_lStartInterleave > 0)
		Sleep(m_lStartInterleave);
	if (m_lTxStartChars > 1)
	{
		for (long lIndex = 0; lIndex < m_lTxStartChars; lIndex ++)
			if (!SendChar(m_cStartChar, m_lLoopBackTimeOut))
				return false;
		return true;
	}
	else
		if (SendChar(m_cStartChar, m_lLoopBackTimeOut))
			return true;
	return false;
}

bool CXBusCommPort::ReceiveMessageHeader(SXBusMessage* psMessage)
{
	if (m_bFilterStartChars)
	{
		*((unsigned char*)&psMessage->sHeader) = m_ucNoStartByte;
		if (ReceiveData((unsigned char*)&psMessage->sHeader, 3, m_lHeaderTimeOut))
			return true;
	}
	else
		if (ReceiveData((unsigned char*)&psMessage->sHeader, 4, m_lHeaderTimeOut))
			return true;
	return false;
}

bool CXBusCommPort::SendMessageHeader(SXBusMessage* psMessage)
{
	if (SendData((unsigned char*)&psMessage->sHeader, 4, m_lLoopBackTimeOut))
		return true;
	return false;
}

bool CXBusCommPort::ReceiveMessageData(SXBusMessage* psMessage)
{
	if ((psMessage->sHeader.ucData == 1) && (!psMessage->sHeader.ucRead))
	{
		if (ReceiveData((unsigned char*)psMessage->ucData, psMessage->sHeader.ucDataSize + 1, m_lDataTimeOut))
			return true;
	}
	else
		return true;
	return false;
}

bool CXBusCommPort::SendMessageData(SXBusMessage* psMessage)
{
	if ((psMessage->sHeader.ucData == 1) && (!psMessage->sHeader.ucRead))
	{
		if (SendData((unsigned char*)psMessage->ucData, psMessage->sHeader.ucDataSize + 1, m_lLoopBackTimeOut))
			return true;
	}
	else
		return true;
	return false;
}

bool CXBusCommPort::ReceiveAck()
{
	if (!m_bAck) return true;
	char cAckChar;
	if (ReceiveChar(&cAckChar, m_lAckTimeOut))
		if (cAckChar == m_cAckChar)
			return true;
	return false;
}

bool CXBusCommPort::SendAck()
{
	if (!m_bAck) return true;
	if (SendChar(m_cAckChar, m_lLoopBackTimeOut))
		return true;
	return false;
}

