async function updateDashboard() {
    try {
        const response = await fetch('/api/status');
        const data = await response.json();

        document.getElementById('ping-val').innerText = data.ping;
        document.getElementById('servers-val').innerText = data.servers;

        const lavaElement = document.getElementById('lavalink-val');
        lavaElement.innerText = data.lavalinkText;

        if (data.lavalinkConnected) {
            lavaElement.className = 'status-green';
        } else {
            lavaElement.className = 'status-red';
        }
    } catch (err) {
        console.error("Dashboard update framework error: ", err);
    }
}

setInterval(updateDashboard, 3500);