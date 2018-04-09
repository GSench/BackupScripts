set dst=E:\
for %%* in (.) do set CurrDirName=%%~nx*
robocopy . %dst%\%CurrDirName% %* /MIR /V
pause