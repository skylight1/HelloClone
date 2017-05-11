from flask import Flask
from opentok import OpenTok, OutputModes,ArchiveModes
from opentok import MediaModes
from flask import request, jsonify, json
import json
API_KEY ="45837512"
SERCET_KEY="d1959c32db76c05336ac8fcf5807efc87f280b41"

app = Flask(__name__)

opentok = OpenTok(API_KEY, SERCET_KEY)

@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/session/create')
def session_create():

    #session = opentok.create_session()

    # A session that uses the OpenTok Media Router:
    session = opentok.create_session(media_mode=MediaModes.routed)

    # An automatically archived session:
    #session = opentok.create_session(media_mode=MediaModes.routed, archive_mode=ArchiveModes.always)
    # Store this session ID in the database

    session_id = session.session_id

    token = session.generate_token()

    return jsonify({"session_id": session_id, "token": token})


@app.route('/token/create',methods=["GET","POST"])
def token_create():
    # Generate a Token from just a session_id (fetched from a database)
    session_id  = request.values.get("session_id")
    token = opentok.generate_token(session_id)
    # Generate a Token by calling the method on the Session (returned from create_session)
    #token = session.generate_token()

    #from opentok import Roles
    # Set some options in a token
    #token = session.generate_token(role=Roles.moderator, expire_time=int(time.time()) + 10,   data=u'name=Johnny')

    return jsonify({"session_id": session_id, "token":token})

@app.route('/session/start-archive',methods=["GET","POST"])
def session_start_archive():
    session_id = request.values.get("session_id")
    print session_id
    archive = opentok.start_archive(session_id,has_audio=True, has_video=True)
    print archive.id
    return archive.json()

@app.route('/session/stop-archive',methods=["GET","POST"])
def session_stop_archive():
    archive_id = request.values.get("archive_id")
    print archive_id
    opentok.stop_archive(archive_id)
    archive = opentok.get_archive(archive_id)
    #nameurl = archive.projectId + "/" + archive.id + "/archive.mp4"
    archive.url="https://s3.amazonaws.com/clone-api/" + str(archive.partner_id) +"/" + archive.id +"/archive.mp4"

    return archive.json()

#
# {
#     "status": "started",
#     "output_mode": null,
#     "name": null,
#     "url": null,
#     "created_at": "2017-05-11T16:25:54+00:00",
#     "has_video": true,
#     "session_id": "2_MX40NTgzNzUxMn5-MTQ5NDUxODAxMDgzOX5YVmJMdWxoakZkb255cHYzUy9zYUlrY09-fg",
#     "id": "52987cf2-01f4-4559-b8b9-167a690f1c1a",
#     "duration": 0,
#     "partner_id": 45837512,
#     "has_audio": true,
#     "size": 0
# }
@app.route("/history")
def history():
    page = 1
    offset = (page - 1) * 5000
    archives = opentok.get_archives(offset=offset, count=5)

    list =[]
    # print archives.items
    for item in archives.items:
        item.url = "https://s3.amazonaws.com/clone-api/" + str(
            item.partner_id) + "/" + item.id + "/archive.mp4"
        temp = {
                "status": item.status,
                "name": item.name,
                "url": item.url,
                "created_at":item.created_at,
                "has_video": item.has_video,
                "session_id": item.session_id,
                "id": item.id,
                "duration": item.duration,
                "partner_id": item.partner_id,
                "has_audio": item.has_audio,
                "size": item.size
            }
        list.append(temp)

    return jsonify({"archives": list})

if __name__ == '__main__':
    app.run()
