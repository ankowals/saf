#include <IE.au3>

Local $url = $CmdLine[1]
Local $username = $CmdLine[2]
Local $password = $CmdLine[3]

Local $hWnd = WinActivate("[CLASS:IEFrame]", "Internet Explorer")
Local $oIE = _IEAttach($hWnd, "hwnd")
Sleep(250)
_IENavigate($oIE, $url, 0)

WinWait("Windows Security", "", 10)
If WinActive("Windows Security", "") Then
   Sleep(250)
   Send($username)
   Send("{TAB}")
   Sleep(250)
   Send($password)
   Send("{ENTER}")
EndIf