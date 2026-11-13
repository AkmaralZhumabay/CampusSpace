const roomList = document.getElementById("roomList");
const roomDetails = document.getElementById("roomDetails");
const roomPoints = document.querySelectorAll(".map-hotspot");

const bookingForm = document.getElementById("bookingForm");
const studentSelect = document.getElementById("studentSelect");
const selectedRoomInput = document.getElementById("selectedRoom");
const bookingMessage = document.getElementById("bookingMessage");
const bookingsList = document.getElementById("bookingsList");

let rooms = [];
let selectedRoom = null;

// Find a room using both its number and block
function findRoomForPoint(point) {
    const number = point.dataset.roomName.trim().toUpperCase();
    const block = point.dataset.block.trim().toUpperCase();

    return rooms.find(room => {
        const roomNumber = room.name.trim().toUpperCase();
        const location = (room.location || "").toUpperCase();

        return roomNumber === number &&
            location.includes(block);
    });
}

// Display booking messages
function showMessage(message, success = false) {
    bookingMessage.textContent = message;
    bookingMessage.className = success
        ? "message success"
        : "message error";
}

// Load only rooms registered on the map
async function loadRooms() {
    try {
        const response = await fetch("/api/rooms");

        if (!response.ok) {
            throw new Error("Failed to load rooms");
        }

        const allRooms = await response.json();

        rooms = allRooms.filter(room => {
            return Array.from(roomPoints).some(point => {
                const sameNumber =
                    room.name.trim().toUpperCase() ===
                    point.dataset.roomName.trim().toUpperCase();

                const sameBlock =
                    (room.location || "").toUpperCase()
                        .includes(point.dataset.block.toUpperCase());

                return sameNumber && sameBlock;
            });
        });

        renderRooms();
        syncMapAvailability();

        if (selectedRoom) {
            selectedRoom = rooms.find(
                room => room.id === selectedRoom.id
            ) || null;

            if (!selectedRoom) {
                selectedRoomInput.value = "";
                roomDetails.textContent = "Select a room from the map or list.";
            }
        }

    } catch (error) {
        roomList.textContent = error.message;
        console.error("Room loading error:", error);
    }
}

// Render available room cards
function renderRooms() {
    roomList.replaceChildren();

    if (rooms.length === 0) {
        roomList.textContent = "No mapped rooms available yet.";
        return;
    }

    rooms.forEach(room => {
        const card = document.createElement("div");
        card.className = "room-card";

        const name = document.createElement("strong");
        name.textContent = `${room.location} / ${room.name}`;

        const details = document.createElement("p");
        details.textContent =
            `Capacity: ${room.capacity}`;

        card.append(name, details);

        card.addEventListener("click", () => {
            selectRoom(room);
        });

        roomList.appendChild(card);
    });
}

// Synchronize map points with database rooms
function syncMapAvailability() {
    roomPoints.forEach(point => {
        const room = findRoomForPoint(point);

        point.classList.toggle("inactive", !room);
        point.disabled = !room;

        point.title = room
            ? `${point.dataset.block} / ${room.name} - Select room`
            : "Room is not registered yet";
    });
}

// Select a room and display its details
function selectRoom(room) {
    selectedRoom = room;
    selectedRoomInput.value = `${room.location} / ${room.name}`;

    roomDetails.replaceChildren();

    const title = document.createElement("h3");
    title.textContent = room.name;

    const capacity = document.createElement("p");
    capacity.textContent = `Capacity: ${room.capacity}`;

    const location = document.createElement("p");
    location.textContent = `Location: ${room.location}`;

    const equipment = document.createElement("p");
    equipment.textContent =
        `Projector: ${room.hasProjector ? "Yes" : "No"} | ` +
        `Whiteboard: ${room.hasWhiteboard ? "Yes" : "No"}`;

    roomDetails.append(title, capacity, location, equipment);

    // Highlight only the matching room in the correct block
    roomPoints.forEach(point => {
        const sameRoom =
            point.dataset.roomName.trim().toUpperCase() ===
            room.name.trim().toUpperCase();

        const sameBlock =
            (room.location || "").toUpperCase()
                .includes(point.dataset.block.toUpperCase());

        point.classList.toggle(
            "selected",
            sameRoom && sameBlock
        );
    });
}

// Load students into dropdown
async function loadStudents() {
    try {
        const response = await fetch("/api/students");

        if (!response.ok) {
            throw new Error("Failed to load students");
        }

        const students = await response.json();

        studentSelect.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select student";
        studentSelect.appendChild(placeholder);

        students.forEach(student => {
            const option = document.createElement("option");
            option.value = student.id;
            option.textContent = student.fullName;
            studentSelect.appendChild(option);
        });

        if (students.length === 1) {
            studentSelect.value = String(students[0].id);
        }

    } catch (error) {
        showMessage(error.message);
        console.error("Student loading error:", error);
    }
}

// Load bookings for the selected student
async function loadBookings() {
    bookingsList.replaceChildren();

    if (!studentSelect.value) {
        bookingsList.textContent =
            "Select a student to view bookings.";
        return;
    }

    try {
        const response = await fetch(
            `/api/bookings/student/${studentSelect.value}`
        );

        if (!response.ok) {
            throw new Error("Failed to load bookings");
        }

        const bookings = await response.json();

        if (bookings.length === 0) {
            bookingsList.textContent = "No bookings yet.";
            return;
        }

        bookings.forEach(booking => {
            const card = document.createElement("div");
            card.className = "booking-card";

            const title = document.createElement("h3");
            title.textContent =
                `${booking.room.location} / ${booking.room.name}`;

            const times = document.createElement("p");
            times.textContent =
                `${booking.startTime.replace("T", " ")} — ` +
                booking.endTime.replace("T", " ");

            const status = document.createElement("p");
            status.textContent = `Status: ${booking.status}`;

            card.append(title, times, status);

            if (booking.status === "CONFIRMED") {
                const cancelButton =
                    document.createElement("button");

                cancelButton.textContent = "Cancel Booking";
                cancelButton.className = "cancel-btn";

                cancelButton.addEventListener("click", () => {
                    cancelBooking(booking.id);
                });

                card.appendChild(cancelButton);
            }

            bookingsList.appendChild(card);
        });

    } catch (error) {
        bookingsList.textContent = error.message;
        console.error("Booking loading error:", error);
    }
}

// Cancel existing booking
async function cancelBooking(id) {
    if (!confirm("Cancel this booking?")) {
        return;
    }

    try {
        const response = await fetch(
            `/api/bookings/${id}/cancel`,
            { method: "PUT" }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Cancellation failed");
        }

        showMessage("Booking cancelled successfully!", true);
        await loadBookings();
        await loadNotifications();

    } catch (error) {
        showMessage(error.message);
    }
}

// Create a booking
bookingForm.addEventListener("submit", async event => {
    event.preventDefault();

    if (!selectedRoom) {
        showMessage("Please select a room.");
        return;
    }

    if (!studentSelect.value) {
        showMessage("Please select a student.");
        return;
    }

    const date = document.getElementById("bookingDate").value;
    const start = document.getElementById("startTime").value;
    const end = document.getElementById("endTime").value;

    if (!date || !start || !end) {
        showMessage("Please complete all booking fields.");
        return;
    }

    const startDateTime = `${date}T${start}:00`;
    const endDateTime = `${date}T${end}:00`;

    if (new Date(startDateTime) >= new Date(endDateTime)) {
        showMessage("End time must be after start time.");
        return;
    }

    const bookButton = document.getElementById("bookBtn");
    bookButton.disabled = true;

    try {
        const response = await fetch("/api/bookings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                roomId: selectedRoom.id,
                studentId: Number(studentSelect.value),
                startTime: startDateTime,
                endTime: endDateTime
            })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Booking failed");
        }

        showMessage(
            `Booking #${result.id} confirmed successfully!`,
            true
        );

        await loadBookings();

    } catch (error) {
        showMessage(error.message);
    } finally {
        bookButton.disabled = false;
    }
});

// Handle clicks on the map
roomPoints.forEach(point => {
    point.addEventListener("click", () => {
        const room = findRoomForPoint(point);

        if (!room) {
            showMessage("This room is not registered yet.");
            return;
        }

        selectRoom(room);
    });
});

// Refresh rooms
document.getElementById("loadRoomsBtn")
    .addEventListener("click", loadRooms);

// Update bookings after selecting a student
studentSelect.addEventListener("change", loadBookings);

// Initialize application
async function initializeApp() {
    await loadRooms();
    await loadNotifications();
    await loadStudents();
    await loadBookings();
}


// Strategy Pattern: search rooms using Java backend
const searchForm = document.getElementById("searchForm");
const searchMessage = document.getElementById("searchMessage");
const resetSearchBtn = document.getElementById("resetSearchBtn");

searchForm.addEventListener("submit", async event => {
    event.preventDefault();

    const people = Number(
        document.getElementById("peopleCount").value
    );

    const strategy = document.getElementById(
        "searchStrategy"
    ).value;

    if (!Number.isInteger(people) ||
        people <= 0 || people > 1000) {
        searchMessage.textContent =
            "Enter a valid number of students (1-1000).";
        return;
    }

    const searchButton = document.getElementById("searchBtn");
    searchButton.disabled = true;

    try {
        const url =
            `/api/search/rooms?people=${people}` +
            `&strategy=${encodeURIComponent(strategy)}`;

        const response = await fetch(url);
        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Search failed");
        }

        // Only display rooms represented on the current map.
        // Preserve the sorting returned by the Java Strategy.
        const matchedRooms = result.filter(room =>
            Array.from(roomPoints).some(point =>
                room.name.trim().toUpperCase() ===
                    point.dataset.roomName.trim().toUpperCase() &&
                (room.location || "").toUpperCase().includes(
                    point.dataset.block.toUpperCase()
                )
            )
        );

        roomList.replaceChildren();

        matchedRooms.forEach(room => {
            const card = document.createElement("div");
            card.className = "room-card";

            const title = document.createElement("strong");
            title.textContent =
                `${room.location} / ${room.name}`;

            const capacity = document.createElement("p");
            capacity.textContent =
                `Capacity: ${room.capacity}`;

            card.append(title, capacity);

            card.addEventListener("click", () => {
                selectRoom(room);
            });

            roomList.appendChild(card);
        });

        searchMessage.textContent =
            `Found ${matchedRooms.length} suitable rooms.`;

        if (matchedRooms.length === 0) {
            roomList.textContent =
                "No suitable rooms on this floor.";
        }

        // Mark matching rooms on the map
        roomPoints.forEach(point => {
            const matched = matchedRooms.some(room =>
                room.name.trim().toUpperCase() ===
                    point.dataset.roomName.trim().toUpperCase() &&
                (room.location || "").toUpperCase().includes(
                    point.dataset.block.toUpperCase()
                )
            );

            point.classList.toggle("search-match", matched);
        });

    } catch (error) {
        searchMessage.textContent = error.message;
    } finally {
        searchButton.disabled = false;
    }
});

resetSearchBtn.addEventListener("click", () => {
    roomPoints.forEach(point => {
        point.classList.remove("search-match");
    });

    searchMessage.textContent = "";
    renderRooms();
});


const notificationsList =
    document.getElementById("notificationsList");

const refreshNotificationsBtn =
    document.getElementById("refreshNotificationsBtn");

async function loadNotifications() {
    try {
        const response = await fetch("/api/notifications");

        if (!response.ok) {
            throw new Error("Failed to load notifications");
        }

        const notifications = await response.json();

        notificationsList.replaceChildren();

        if (notifications.length === 0) {
            notificationsList.textContent =
                "No notifications yet.";
            return;
        }

        notifications.slice().reverse().forEach(message => {
            const item = document.createElement("div");

            item.className = "notification-item";
            item.textContent = message;

            notificationsList.appendChild(item);
        });

    } catch (error) {
        notificationsList.textContent = error.message;
        console.error("Notification error:", error);
    }
}

refreshNotificationsBtn.addEventListener(
    "click",
    loadNotifications
);

loadNotifications();


// Schedule CSV import - Adapter Pattern

const scheduleImportForm =
    document.getElementById("scheduleImportForm");

const scheduleFile =
    document.getElementById("scheduleFile");

const scheduleMessage =
    document.getElementById("scheduleMessage");

const scheduleList =
    document.getElementById("scheduleList");

const refreshScheduleBtn =
    document.getElementById("refreshScheduleBtn");

async function loadSchedule() {
    try {
        const response = await fetch("/api/schedule");

        if (!response.ok) {
            throw new Error("Could not load schedule");
        }

        const entries = await response.json();

        scheduleList.replaceChildren();

        if (entries.length === 0) {
            scheduleList.textContent = "No scheduled classes.";
            return;
        }

        entries.forEach(entry => {
            const card = document.createElement("div");
            card.className = "schedule-card";

            const title = document.createElement("strong");
            title.textContent = entry.courseName;

            const room = document.createElement("p");
            room.textContent =
                `Room: ${entry.room.location} / ${entry.room.name}`;

            const time = document.createElement("p");
            time.textContent =
                `From: ${entry.startTime.replace("T", " ")} ` +
                `To: ${entry.endTime.replace("T", " ")}`;

            card.append(title, room, time);
            scheduleList.appendChild(card);
        });

    } catch (error) {
        scheduleList.textContent = error.message;
    }
}

scheduleImportForm.addEventListener(
    "submit",
    async event => {
        event.preventDefault();

        if (!scheduleFile.files.length) {
            scheduleMessage.textContent =
                "Please select a CSV file.";
            return;
        }

        const file = scheduleFile.files[0];
        const button =
            document.getElementById("importScheduleBtn");

        const formData = new FormData();
        formData.append("file", file);

        button.disabled = true;
        scheduleMessage.textContent = "Importing schedule...";

        try {
            const response = await fetch(
                "/api/schedule/import",
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.json();

            if (!response.ok) {
                throw new Error(
                    result.error || "Schedule import failed"
                );
            }

            scheduleMessage.textContent =
                `Successfully imported ${result.length} classes.`;

            scheduleMessage.style.color = "green";

            scheduleImportForm.reset();
            await loadSchedule();

        } catch (error) {
            scheduleMessage.textContent = error.message;
            scheduleMessage.style.color = "red";
        } finally {
            button.disabled = false;
        }
    }
);

refreshScheduleBtn.addEventListener(
    "click",
    loadSchedule
);

loadSchedule();

initializeApp();
