from flask import Flask
from opentok import OpenTok
from opentok import MediaModes
from flask import request, jsonify, json

API_KEY ="45837512"
SERCET_KEY="d1959c32db76c05336ac8fcf5807efc87f280b41"

app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/session/create')
def session_create():
    opentok = OpenTok(API_KEY, SERCET_KEY)
    #session = opentok.create_session()

    # A session that uses the OpenTok Media Router:
    session = opentok.create_session(media_mode=MediaModes.routed)

    # An automatically archived session:
    #session = opentok.create_session(media_mode=MediaModes.routed, archive_mode=ArchiveModes.always)
    # Store this session ID in the database

    session_id = session.session_id

    token = session.generate_token()

    return jsonify({"session_id": session_id, "token": token})


@app.route('/token/create')
def token_create():
    opentok = OpenTok(API_KEY, SERCET_KEY)
    # Generate a Token from just a session_id (fetched from a database)
    session_id  = request.values.get("session_id")
    token = opentok.generate_token(session_id)
    # Generate a Token by calling the method on the Session (returned from create_session)
    #token = session.generate_token()

    #from opentok import Roles
    # Set some options in a token
    #token = session.generate_token(role=Roles.moderator, expire_time=int(time.time()) + 10,   data=u'name=Johnny')

    return jsonify({"session_id": session_id, "token":token})

if __name__ == '__main__':
    app.run()
