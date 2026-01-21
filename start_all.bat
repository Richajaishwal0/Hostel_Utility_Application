@echo off
echo Starting Hostel Utility Application
echo ===================================

cd /d "C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app"

echo Compiling Java modules...
javac module1-socket\*.java
javac module2-rmi\*.java
javac module3-rest\*.java
javac module4-p2p\*.java
javac module5-shared-memory\*.java

echo Starting servers...
start "Socket Server" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module1-socket && java ComplaintServer"
timeout /t 2 /nobreak >nul
start "Socket Bridge" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module1-socket && java ComplaintBridge"
timeout /t 2 /nobreak >nul
start "RMI Server" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module2-rmi && java RoomInfoServer"
timeout /t 2 /nobreak >nul
start "RMI Bridge" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module2-rmi && java RoomInfoBridge"
timeout /t 2 /nobreak >nul
start "REST Server" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module3-rest && java NoticeServer"
timeout /t 2 /nobreak >nul
start "P2P Peer 1" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module4-p2p && java P2PPeer 9001"
timeout /t 2 /nobreak >nul
start "P2P Peer 2" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module4-p2p && java P2PPeer 9002"
timeout /t 2 /nobreak >nul
start "P2P Bridge" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module4-p2p && java P2PBridge"
timeout /t 2 /nobreak >nul
start "Shared Memory Server" cmd /k "cd /d C:\Users\richa\OneDrive\CLASSNOTES-SEM6\Distributed_Lab\Project-1\hostel-utility-app\module5-shared-memory && java SharedMemoryFeedbackServer"

echo All servers started!
echo Open ui\index.html in your browser
pause