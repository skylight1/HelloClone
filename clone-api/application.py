#!flask/bin/python
import sys
#sys.path.insert(0, '/var/www/productions/clone-api')

from app import app as application

application.debug=True
if __name__ == '__main__':
    #/test
    application.run(host='0.0.0.0', debug=True)