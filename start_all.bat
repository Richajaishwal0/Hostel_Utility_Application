@echo off
echo Starting Hostel Utility Application - All Modules
echo ================================================

echo Compiling Java modules...
cd module1-socket && javac *.java && cd ..
cd module2-rmi && javac *.java && cd ..
cd module3-rest && javac *.java && cd ..
cd module4-p2p && javac *.java && cd ..
cd module5-shared-memory && javac *.java && cd ..

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
start "P2P Bridge" cmd /k "cd module4-p2p && java P2PBridge"
timeout /t 2 /nobreak >nul
start "Shared Memory Server" cmd /k "cd module5-shared-memory && java SharedMemoryFeedbackServer"
timeout /t 2 /nobreak >nul

echo All 10 servers started successfully!
echo - Socket Server (port 8080)
echo - Socket Bridge (port 8082)
echo - RMI Server (port 1099)
echo - RMI Bridge (port 8083)
echo - REST Server (port 8081)
echo - P2P Peer 1 (port 9001)
echo - P2P Peer 2 (port 9002)
echo - P2P Bridge (port 8084)
echo - Shared Memory Server (port 8084)
echo.
echo Open ui/index.html in your browser to use the application.
pause