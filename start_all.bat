@echo off
echo Starting Hostel Utility Application - All Modules
echo ================================================

echo Compiling Java modules...
cd module1-socket && javac *.java && cd ..
cd module2-rmi && javac *.java && cd ..
cd module3-rest && javac *.java && cd ..
cd module4-p2p && javac *.java && cd ..

echo Skipping C modules (requires GCC compiler)...

echo Starting all servers...
start "Socket Server" cmd /k "cd module1-socket && java ComplaintServer"
timeout /t 2 /nobreak >nul
start "Socket Bridge" cmd /k "cd module1-socket && java ComplaintBridge"
timeout /t 2 /nobreak >nul
start "RMI Server" cmd /k "cd module2-rmi && java RoomInfoServer"
timeout /t 2 /nobreak >nul
start "RMI Bridge" cmd /k "cd module2-rmi && java RoomInfoBridge"
timeout /t 2 /nobreak >nul
start "REST Server" cmd /k "cd module3-rest && java NoticeServer"
timeout /t 2 /nobreak >nul
start "P2P Peer 1" cmd /k "cd module4-p2p && java P2PPeer 9001"
timeout /t 2 /nobreak >nul
start "P2P Peer 2" cmd /k "cd module4-p2p && java P2PPeer 9002"
timeout /t 2 /nobreak >nul
echo Shared Memory Server skipped (C module)

echo All servers started! Open ui/index.html in your browser.
pause