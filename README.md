# Hostel Utility Application - Distributed Systems Project

## Overview
A complete hostel management application demonstrating **all 5 distributed communication models** with a functional web UI and **justified in-memory data management**.

## In-Memory Storage Design Philosophy

**Key Principle**: Not all distributed data needs persistence. This application demonstrates appropriate data lifecycle management where in-memory storage provides optimal performance and aligns with business requirements.

**See [IN_MEMORY_JUSTIFICATION.md](IN_MEMORY_JUSTIFICATION.md) for detailed analysis of why each module uses in-memory storage.**

## Project Structure
```
hostel-utility-app/
├── module1-socket/          # Socket Programming - Complaint Management
├── module2-rmi/             # Java RMI - Room Information Service  
├── module3-rest/            # REST API - Notice Board System
├── module4-p2p/             # P2P - Resource Sharing System
├── module5-shared-memory/   # Shared Memory - Mess Feedback Counter
├── ui/                      # Web User Interface
├── Makefile                 # Build and run automation
└── README.md               # This file
```

## Modules Implementation

### Module 1: Socket Programming - Complaint Management System
- **Technology**: Java Socket Programming
- **Functionality**: Students submit hostel complaints via UI
- **Features**: 
  - Multi-client server handling
  - Complaint submission and listing
  - In-memory storage using synchronized List
- **Ports**: 8080

### Module 2: Java RMI - Room Information Service  
- **Technology**: Java Remote Method Invocation
- **Functionality**: Students search for room details and warden information
- **Features**:
  - Remote method calls
  - Room occupant information
  - Warden contact details
- **Ports**: 1099 (RMI Registry)

### Module 3: REST API - Notice Board System
- **Technology**: HTTP REST API (Java HttpServer)
- **Functionality**: Admin adds notices, students view them
- **Features**:
  - RESTful endpoints (GET, POST)
  - JSON response format
  - Stateless communication
- **Ports**: 8081

### Module 4: P2P - Resource Sharing System
- **Technology**: Peer-to-Peer networking
- **Functionality**: Students share academic resources directly
- **Features**:
  - Decentralized file sharing
  - Peer discovery and communication
  - Direct file transfer
- **Ports**: 9001, 9002 (configurable)

### Module 5: Shared Memory - Mess Feedback Counter
- **Technology**: System V Shared Memory + Semaphores (C)
- **Functionality**: Live feedback counting with synchronization
- **Features**:
  - Shared memory segments
  - Semaphore synchronization
  - Race condition prevention
- **IPC Keys**: SHM_KEY=9999, SEM_KEY=8888

## User Interface
- **Technology**: HTML5, CSS3, JavaScript
- **Features**:
  - Responsive design
  - Module-based navigation
  - Real-time updates
  - Form validation

## In-Memory Storage Justification

### Why In-Memory Storage is Appropriate:

1. **Module 1 (Socket)**: Complaint queue - temporary storage for processing
2. **Module 2 (RMI)**: Room directory - frequently accessed, small dataset
3. **Module 3 (REST)**: Notice cache - notices are temporary announcements
4. **Module 4 (P2P)**: File index - dynamic peer discovery, no central persistence needed
5. **Module 5 (Shared Memory)**: Live counters - real-time data, resets are acceptable

### System Behavior on Restart:
- Complaint queue resets (acceptable for demo)
- Room data reinitializes with sample data
- Notices clear (admin can re-add important ones)
- P2P network rebuilds peer connections
- Feedback counters reset to zero

## Quick Start

### Prerequisites
- Java JDK 8+
- GCC compiler
- Web browser
- Make utility

### Build and Run
```bash
# Compile all modules
make all

# Start complete application
make demo

# Open ui/index.html in web browser
```

### Individual Module Testing
```bash
make test-socket          # Test complaint system
make test-rmi            # Test room information
make test-rest           # Test notice board
make test-p2p            # Test resource sharing
make test-shared-memory  # Test feedback system
```

## Usage Instructions

### 1. Complaint Management (Socket)
- Fill complaint form with room number, category, description
- Submit complaint and receive confirmation
- View all submitted complaints

### 2. Room Information (RMI)
- Enter room number to search
- View occupant names and warden details
- Browse all available rooms

### 3. Notice Board (REST)
- Admin: Add new notices with title and message
- Student: View all posted notices
- Notices display with timestamps

### 4. Resource Sharing (P2P)
- Upload files to share with peers
- Search for files across the network
- Download files from other peers

### 5. Mess Feedback (Shared Memory)
- Submit feedback: Good, Average, or Poor
- View live counter updates
- Multiple users can submit simultaneously

## Technical Highlights

### Distributed Systems Concepts Demonstrated:
- **Client-Server Architecture** (Socket, RMI, REST)
- **Peer-to-Peer Communication** (P2P)
- **Shared Memory IPC** (Shared Memory)
- **Synchronization** (Semaphores, Concurrent Collections)
- **State Management** (In-memory data structures)
- **Concurrency** (Multi-threading, Process communication)

### Key Learning Outcomes:
- Socket programming and multi-client handling
- Remote method invocation and distributed objects
- RESTful API design and stateless communication
- Decentralized architectures and peer discovery
- Inter-process communication and synchronization

## Troubleshooting

### Common Issues:
1. **Port conflicts**: Change ports in source code if needed
2. **Java classpath**: Ensure Java files are compiled in correct directories
3. **Shared memory**: Run `make stop` to clean up IPC resources
4. **Browser CORS**: Serve UI from local web server if needed

### Cleanup:
```bash
make clean    # Remove compiled files
make stop     # Stop all running services
```

## Demo Script

1. **Start Application**: `make demo`
2. **Open UI**: Launch `ui/index.html` in browser
3. **Test Each Module**:
   - Submit a complaint (Socket)
   - Search room information (RMI)  
   - Add and view notices (REST)
   - Upload and search files (P2P)
   - Submit feedback and view counters (Shared Memory)

## Group Evaluation Points

✅ **All 5 modules implemented**  
✅ **Different distributed communication models**  
✅ **Functional web UI**  
✅ **In-memory data management**  
✅ **Runnable and demo-ready**  
✅ **Proper justification for in-memory storage**  
✅ **Concurrent access handling**  
✅ **System design documentation**

---

**Project demonstrates comprehensive understanding of distributed systems concepts through practical implementation of a realistic hostel utility application.**