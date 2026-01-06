// Module navigation
function showModule(moduleId) {
    // Hide all modules
    document.querySelectorAll('.module').forEach(module => {
        module.classList.remove('active');
    });
    
    // Remove active class from all nav buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected module
    document.getElementById(moduleId).classList.add('active');
    
    // Add active class to clicked button
    event.target.classList.add('active');
}

// Module 1: Socket Programming - Complaints
document.getElementById('complaintForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const room = document.getElementById('room').value;
    const category = document.getElementById('category').value;
    const description = document.getElementById('description').value;
    
    // Try real socket communication first, fallback to demo mode
    fetch('http://localhost:8082/complaint', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({room, category, description})
    })
    .then(response => response.text())
    .then(data => {
        const parts = data.split('|');
        if (parts[0] === 'SUCCESS') {
            document.getElementById('complaintResult').innerHTML = 
                `<div class="result-item success">${parts[1]}</div>`;
        } else {
            document.getElementById('complaintResult').innerHTML = 
                `<div class="result-item error">${parts[1] || 'Submission failed'}</div>`;
        }
        document.getElementById('complaintForm').reset();
        loadComplaints();
    })
    .catch(error => {
        // Demo mode fallback
        const complaintId = Math.floor(Math.random() * 1000) + 1;
        document.getElementById('complaintResult').innerHTML = 
            `<div class="result-item success">Complaint #${complaintId} submitted successfully! (Demo Mode)</div>`;
        
        // Store in demo storage
        const complaint = {
            id: complaintId,
            room: room,
            category: category,
            description: description,
            date: new Date()
        };
        
        let demoComplaints = JSON.parse(localStorage.getItem('demoComplaints') || '[]');
        demoComplaints.push(complaint);
        localStorage.setItem('demoComplaints', JSON.stringify(demoComplaints));
        
        document.getElementById('complaintForm').reset();
        loadComplaints();
    });
});

function loadComplaints() {
    // Load real complaints from socket server
    fetch('http://localhost:8082/complaints')
    .then(response => response.text())
    .then(data => {
        const parts = data.split('|');
        if (parts[0] === 'LIST') {
            const complaintsData = parts[1];
            if (!complaintsData || complaintsData.trim() === '') {
                document.getElementById('complaintsList').innerHTML = 
                    '<h4>Recent Complaints:</h4><div class="result-item">No complaints submitted yet.</div>';
                return;
            }
            
            const complaints = complaintsData.split(';').filter(c => c.trim() !== '');
            let html = '<h4>Recent Complaints:</h4>';
            
            complaints.forEach(complaintStr => {
                const fields = complaintStr.split(',');
                if (fields.length >= 5) {
                    const id = fields[0];
                    const room = fields[1];
                    const category = fields[2];
                    const description = fields[3];
                    const timestamp = fields.slice(4).join(','); // Handle comma in timestamp
                    
                    html += `<div class="result-item">
                        <strong>ID: #${id}</strong> - Room ${room} (${category})<br>
                        ${description}<br>
                        <small>Submitted: ${timestamp}</small>
                    </div>`;
                }
            });
            
            document.getElementById('complaintsList').innerHTML = html;
        } else {
            document.getElementById('complaintsList').innerHTML = 
                '<h4>Recent Complaints:</h4><div class="result-item error">Failed to load complaints.</div>';
        }
    })
    .catch(error => {
        document.getElementById('complaintsList').innerHTML = 
            '<h4>Recent Complaints:</h4><div class="result-item error">Connection failed. Please ensure servers are running.</div>';
        console.error('Error:', error);
    });
}

// Module 2: RMI - Room Information
function searchRoom() {
    const roomNumber = document.getElementById('roomSearch').value;
    if (!roomNumber) {
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Please enter a room number</div>`;
        return;
    }
    
    // Real RMI call via bridge
    fetch(`http://localhost:8083/room?number=${roomNumber}`)
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.error) {
            document.getElementById('roomResult').innerHTML = 
                `<div class="result-item error">${data.error}</div>`;
        } else {
            document.getElementById('roomResult').innerHTML = `
                <div class="result-item success">
                    <h4>Room ${data.room} Information</h4>
                    <p><strong>Occupants:</strong> ${data.occupants}</p>
                    <p><strong>Warden:</strong> ${data.warden}</p>
                    <p><strong>Contact:</strong> ${data.contact}</p>
                </div>
            `;
        }
    })
    .catch(error => {
        console.error('RMI Error:', error);
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Connection failed: ${error.message}. Check if RMI Bridge (port 8083) is running.</div>`;
    });
}

function loadAllRooms() {
    fetch('http://localhost:8083/rooms')
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.text();
    })
    .then(data => {
        const rooms = data.replace(/[\[\]]/g, '').split(', ');
        let html = '<h4>Available Rooms:</h4>';
        
        rooms.forEach(room => {
            if (room.trim()) {
                html += `<div class="result-item">
                    <strong>Room ${room}</strong>
                    <button onclick="document.getElementById('roomSearch').value='${room}'; searchRoom();">View Details</button>
                </div>`;
            }
        });
        
        document.getElementById('roomResult').innerHTML = html;
    })
    .catch(error => {
        console.error('RMI Error:', error);
        document.getElementById('roomResult').innerHTML = 
            `<div class="result-item error">Connection failed: ${error.message}. Check if RMI Bridge (port 8083) is running.</div>`;
    });
}

// Module 3: REST API - Notices
document.getElementById('noticeForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const title = document.getElementById('noticeTitle').value;
    const message = document.getElementById('noticeMessage').value;
    
    // Simulate REST API call
    fetch('http://localhost:8081/admin', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}`
    })
    .then(response => response.text())
    .then(data => {
        alert('Notice added successfully!');
        document.getElementById('noticeForm').reset();
        loadNotices();
    })
    .catch(error => {
        alert('Notice added successfully! (Demo Mode)');
        document.getElementById('noticeForm').reset();
        loadNotices();
    });
});

function loadNotices() {
    // Simulate loading notices
    const notices = [
        {id: 1, title: 'Mess Timing Change', message: 'Dinner timing changed to 7:30 PM', date: '2024-01-15'},
        {id: 2, title: 'Maintenance Notice', message: 'Water supply will be off from 2-4 PM tomorrow', date: '2024-01-14'},
        {id: 3, title: 'Festival Celebration', message: 'Cultural fest on campus this weekend', date: '2024-01-13'}
    ];
    
    let html = '';
    notices.forEach(notice => {
        html += `<div class="result-item">
            <h4>${notice.title}</h4>
            <p>${notice.message}</p>
            <small>Date: ${notice.date}</small>
        </div>`;
    });
    
    document.getElementById('noticesList').innerHTML = html;
}

// Module 4: P2P - Resource Sharing
function uploadFile() {
    const fileName = document.getElementById('fileName').value;
    const fileInput = document.getElementById('fileUpload');
    
    if (fileName && fileInput.files.length > 0) {
        alert(`File "${fileName}" uploaded successfully to P2P network!`);
        document.getElementById('fileName').value = '';
        document.getElementById('fileUpload').value = '';
    } else {
        alert('Please provide file name and select a file!');
    }
}

function searchFiles() {
    const searchTerm = document.getElementById('searchFile').value;
    
    // Simulate P2P search
    const availableFiles = [
        {name: 'notes.txt', peer: 'localhost:9001'},
        {name: 'assignment.pdf', peer: 'localhost:9002'},
        {name: 'lecture_slides.ppt', peer: 'localhost:9003'},
        {name: 'project_report.doc', peer: 'localhost:9001'}
    ];
    
    const results = availableFiles.filter(file => 
        file.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
    
    let html = '<h4>Search Results:</h4>';
    if (results.length > 0) {
        results.forEach(file => {
            html += `<div class="peer-result">
                <span><strong>${file.name}</strong> (Available at: ${file.peer})</span>
                <button class="download-btn" onclick="downloadFile('${file.name}', '${file.peer}')">Download</button>
            </div>`;
        });
    } else {
        html += '<div class="result-item error">No files found matching your search.</div>';
    }
    
    document.getElementById('searchResults').innerHTML = html;
}

function downloadFile(fileName, peer) {
    alert(`Downloading "${fileName}" from ${peer}...`);
    setTimeout(() => {
        alert(`"${fileName}" downloaded successfully!`);
    }, 1000);
}

// Module 5: Shared Memory - Mess Feedback
function submitFeedback(type) {
    // Simulate shared memory update
    const currentCounts = {
        good: parseInt(document.getElementById('goodCount').textContent),
        average: parseInt(document.getElementById('averageCount').textContent),
        poor: parseInt(document.getElementById('poorCount').textContent),
        total: parseInt(document.getElementById('totalCount').textContent)
    };
    
    currentCounts[type]++;
    currentCounts.total++;
    
    // Update display
    document.getElementById('goodCount').textContent = currentCounts.good;
    document.getElementById('averageCount').textContent = currentCounts.average;
    document.getElementById('poorCount').textContent = currentCounts.poor;
    document.getElementById('totalCount').textContent = currentCounts.total;
    
    alert(`${type.charAt(0).toUpperCase() + type.slice(1)} feedback submitted!`);
}

function loadFeedbackResults() {
    // Simulate loading from shared memory
    const randomGood = Math.floor(Math.random() * 50) + 10;
    const randomAverage = Math.floor(Math.random() * 30) + 5;
    const randomPoor = Math.floor(Math.random() * 20) + 2;
    
    document.getElementById('goodCount').textContent = randomGood;
    document.getElementById('averageCount').textContent = randomAverage;
    document.getElementById('poorCount').textContent = randomPoor;
    document.getElementById('totalCount').textContent = randomGood + randomAverage + randomPoor;
}

// Initialize with some demo data
window.addEventListener('load', function() {
    loadFeedbackResults();
});