import json
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

BASE_URL = "http://localhost:8080"

ROOMS = [
    "240K",
    "243K",
    "244K",
    "245",
    "248L",
    "250L",
    "252K",
    "254"
]


def request(method, path, data=None):
    body = None
    headers = {}

    if data is not None:
        body = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"

    req = Request(
        BASE_URL + path,
        data=body,
        headers=headers,
        method=method
    )

    try:
        with urlopen(req) as response:
            return json.load(response)
    except HTTPError as error:
        message = error.read().decode()
        raise RuntimeError(
            f"HTTP {error.code}: {message}"
        )


def main():
    existing = request("GET", "/api/rooms")
    added = 0

    for number in ROOMS:
        found = next(
            (
                room for room in existing
                if (room.get("roomNumber")
                    or room.get("name") or "").upper() == number
                and (
                    room.get("block") == "C1.2"
                    or (
                        not room.get("block")
                        and "C1.2" in (room.get("location") or "")
                    )
                )
            ),
            None
        )

        if found:
            print(f"SKIP: C1.2 / {number} already exists")
            continue

        new_room = request("POST", "/api/rooms", {
            "name": number,
            "capacity": 24,
            "location": "C1.2, Floor 2",
            "hasProjector": False,
            "hasWhiteboard": True
        })

        room_id = new_room["id"]

        request("PUT", f"/api/rooms/{room_id}/metadata", {
            "building": "C1",
            "block": "C1.2",
            "floor": 2,
            "roomNumber": number,
            "capacity": 24,
            "bookable": True,
            "hasProjector": False,
            "hasWhiteboard": True
        })

        print(f"ADDED: C1.2 / {number} (ID {room_id})")
        existing.append(new_room)
        added += 1

    print(f"\nFinished. New rooms: {added}")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, URLError) as error:
        print("ERROR:", error)
