let chart;
const maxDataPoints = 60;

function initChart() {
    const ctx = document.getElementById('trafficChart').getContext('2d');
    
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Token Bucket Passed',
                    data: [],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4
                },
                {
                    label: 'Token Bucket Dropped',
                    data: [],
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4
                },
                {
                    label: 'Leaky Bucket Passed',
                    data: [],
                    borderColor: '#3b82f6',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    borderDash: [5, 5],
                    tension: 0.4
                },
                {
                    label: 'Leaky Bucket Dropped',
                    data: [],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    borderDash: [5, 5],
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            color: '#94a3b8',
            scales: {
                x: {
                    grid: { color: 'rgba(148, 163, 184, 0.1)' },
                    ticks: { color: '#94a3b8' }
                },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(148, 163, 184, 0.1)' },
                    ticks: { color: '#94a3b8' }
                }
            },
            plugins: {
                legend: { labels: { color: '#f8fafc' } }
            },
            animation: {
                duration: 0
            }
        }
    });
}

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const ws = new WebSocket(`${protocol}//${window.location.host}/telemetry`);
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        updateDashboard(data);
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected. Attempting to reconnect...');
        setTimeout(connectWebSocket, 1000);
    };
}

function updateDashboard(data) {
    document.getElementById('tokens-val').innerText = data.bucketLevel;
    document.getElementById('passed-val').innerText = data.passed;
    document.getElementById('dropped-val').innerText = data.dropped;
    document.getElementById('leaky-passed-val').innerText = data.leakyPassed;
    document.getElementById('leaky-dropped-val').innerText = data.leakyDropped;

    chart.data.labels.push(data.timestamp);
    chart.data.datasets[0].data.push(data.passed);
    chart.data.datasets[1].data.push(data.dropped);
    chart.data.datasets[2].data.push(data.leakyPassed);
    chart.data.datasets[3].data.push(data.leakyDropped);

    if (chart.data.labels.length > maxDataPoints) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
        chart.data.datasets[1].data.shift();
        chart.data.datasets[2].data.shift();
        chart.data.datasets[3].data.shift();
    }

    chart.update();
}

function initControls() {
    const capacitySlider = document.getElementById('capacity-slider');
    const refillSlider = document.getElementById('refill-slider');
    const capacityDisplay = document.getElementById('capacity-display');
    const refillDisplay = document.getElementById('refill-display');

    function sendConfig() {
        const config = {
            capacity: parseInt(capacitySlider.value),
            refillRate: parseInt(refillSlider.value)
        };
        
        fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        });
    }

    capacitySlider.addEventListener('input', (e) => {
        capacityDisplay.innerText = e.target.value;
        sendConfig();
    });

    refillSlider.addEventListener('input', (e) => {
        refillDisplay.innerText = e.target.value;
        sendConfig();
    });

    fetch('/api/config')
        .then(res => res.json())
        .then(config => {
            capacitySlider.value = config.capacity;
            capacityDisplay.innerText = config.capacity;
            refillSlider.value = config.refillRate;
            refillDisplay.innerText = config.refillRate;
        });
}

function spawnClients(type) {
    const countInput = document.getElementById(`${type}-count`);
    const count = countInput ? countInput.value : 1;
    
    fetch(`/api/clients/spawn/${type}?count=${count}`, {
        method: 'POST'
    }).then(() => {
        pollClientCount(); // Update count immediately
    }).catch(err => console.error("Error spawning clients:", err));
}

function stopAllClients() {
    fetch('/api/clients/stop', {
        method: 'POST'
    }).then(() => {
        pollClientCount();
    }).catch(err => console.error("Error stopping clients:", err));
}

function pollClientCount() {
    fetch('/api/clients/count')
        .then(res => res.json())
        .then(count => {
            const display = document.getElementById('active-clients-count');
            if (display) {
                display.innerText = count;
            }
        }).catch(err => console.error("Error fetching client count:", err));
}

document.addEventListener('DOMContentLoaded', () => {
    initChart();
    initControls();
    connectWebSocket();
    pollClientCount();
    // Poll client count every 5 seconds to ensure accuracy
    setInterval(pollClientCount, 5000);
});
