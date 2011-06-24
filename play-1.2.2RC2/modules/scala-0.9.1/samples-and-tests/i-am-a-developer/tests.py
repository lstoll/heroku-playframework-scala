#!/usr/bin/python

import unittest
import os
import shutil
import sys
import subprocess
import re
import time
import urllib2
import mechanize
import threading

# --- TESTS

class IamADeveloper(unittest.TestCase):

    def testSimpleProjectCreation(self):
        
        # Well
        step('Hello, I\'m a developer')
        
        self.working_directory = bootstrapWorkingDirectory()
        
        # play new yop
        step('Create a new project')
        
        self.play = callPlay(self, ['new', '%s/yop' % self.working_directory, '--name=YOP', '--with', 'scala'])
        self.assert_(waitFor(self.play, 'The new application will be created'))
        self.assert_(waitFor(self.play, 'OK, the application is created'))
        self.assert_(waitFor(self.play, 'Have fun!'))
        self.play.wait()
        
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app/controllers.scala')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app/views')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app/views/Application')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app/views/Application/index.scala.html')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/app/views/main.scala.html')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/conf')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/conf/routes')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/conf/messages')))
        self.assert_(os.path.exists(os.path.join(self.working_directory, 'yop/conf/application.conf')))

        app = '%s/yop' % self.working_directory

        # Run the newly created application
        step('Run the newly created application')
        
        self.play = callPlay(self, ['run', app])
        self.assert_(waitFor(self.play, 'Module scala is available'))
        self.assert_(waitFor(self.play, 'Listening for HTTP on port 9000'))
        
        # Start a browser
        step('Start a browser')
        
        browser = mechanize.Browser()
        
        # Open the home page
        step('Open the home page')
        
        response = browser.open('http://localhost:9000/')
        self.assert_(waitFor(self.play, "Application 'YOP' is now started !"))
        self.assert_(browser.viewing_html())
        self.assert_(browser.title() == 'Your Scala application is ready!')
        
        html = response.get_data()
        self.assert_(html.count('Your Scala application is ready!'))
        
        # Refresh
        step('Refresh home')
        
        response = browser.reload()
        self.assert_(browser.viewing_html())
        self.assert_(browser.title() == 'Your Scala application is ready!')        
        html = response.get_data()
        self.assert_(html.count('Your Scala application is ready!'))
        
        # Change index action result
        step('Change index action result')
        
        edit(app, 'app/controllers.scala', 11, '        Text("Coucou")')   
        response = browser.reload()
        html = response.get_data()
        self.assert_(html.count('Coucou'))
        
        time.sleep(1)
        
        # Try return type inference
        step('Try return type inference')
        
        edit(app, 'app/controllers.scala', 11, '     "Bob"')    
        response = browser.reload()
        html = response.get_data()
        self.assert_(html.count('Bob'))
        
        time.sleep(1)
        
        # Change return type
        step('Change return type')
        
        edit(app, 'app/controllers.scala', 11, ' 9')   
        response = browser.reload()
        html = response.get_data()      
        self.assert_(html.count('9'))
        
        time.sleep(1)
        
        # Change return type again
        step('Change return type again')
        
        edit(app, 'app/controllers.scala', 11, '  Some(9)')   
        response = browser.reload()
        html = response.get_data()  
        self.assert_(html.count('Some(9)'))
        
        time.sleep(1)
        
        # Create a models.scala file
        step('Create a models.scala file')
        
        create(app, 'app/models.scala')
        insert(app, 'app/models.scala', 1, 'package models')   
        insert(app, 'app/models.scala', 2, 'object A { def name = "COUCOU" }')  
        response = browser.reload()
        html = response.get_data()  
        self.assert_(html.count('Some(9)')) 
        
        time.sleep(1)
        
        # Use a model
        step('Use a model')
        
        edit(app, 'app/controllers.scala', 11, ' models.A.name')   
        response = browser.reload()
        html = response.get_data()  
        self.assert_(html.count('COUCOU'))
        
        time.sleep(1)
        
        # Change model method return type
        step('Change model method return type')
        
        edit(app, 'app/models.scala', 2, 'object A { def name = 88 }')  
        response = browser.reload()
        html = response.get_data()  
        self.assert_(html.count('88'))
        
        time.sleep(1)
        
        # Change model type name
        step('Change model type name')
        
        edit(app, 'app/models.scala', 2, 'object AAA { def name = 88 }')  
        try:
            browser.reload()
            self.fail()
        except urllib2.HTTPError, error:
            self.assert_(browser.viewing_html())
            self.assert_(browser.title() == 'Application error')
            html = ''.join(error.readlines())
            self.assert_(html.count('Compilation error'))
            self.assert_(html.count('value A is not a member of package models'))
            self.assert_(html.count('In /app/controllers.scala (around line 11)'))          
            self.assert_(waitFor(self.play, 'ERROR ~'))
            self.assert_(waitFor(self.play, 'Compilation error (In /app/controllers.scala around line 11)'))
            self.assert_(waitFor(self.play, 'value A is not a member of package models'))
            self.assert_(waitFor(self.play, 'at Invocation.HTTP Request(Play!)'))
            
        # Update controller
        step('Update controller')    
        
        edit(app, 'app/controllers.scala', 11, '  models.AAA.name')   
        response = browser.reload()
        html = response.get_data()  
        self.assert_(html.count('88'))
             
        # Stop the application
        step('Kill play')
        
        killPlay()
        self.play.wait()

    def tearDown(self):
        killPlay()


# --- UTILS

def bootstrapWorkingDirectory():
    test_base = os.path.normpath(os.path.dirname(os.path.realpath(sys.argv[0])))
    working_directory = os.path.join(test_base, 'i-am-working-here')
    if(os.path.exists(working_directory)):
        shutil.rmtree(working_directory)
    os.mkdir(working_directory)
    return working_directory

def callPlay(self, args):
    play_script = os.path.join(self.working_directory, playScript)
    process_args = [play_script] + args
    play_process = subprocess.Popen(process_args,stdout=subprocess.PIPE)
    return play_process

def waitFor(process, pattern):
    timer = threading.Timer(5, timeout, [process])
    timer.start()
    while True:
        line = process.stdout.readline().strip()
        if timeoutOccured:
            return False
        if line == '@KILLED':
            return False
        if line: print line
        if line.count(pattern):
            timer.cancel()
            return True

timeoutOccured = False

def timeout(process):
    global timeoutOccured
    print '@@@@ TIMEOUT !'
    timeoutOccured = True
    killPlay()

def killPlay():
    try:
        urllib2.urlopen('http://localhost:9000/@kill')
    except:
        pass

def step(msg):
    print
    print '# --- %s' % msg
    print

def edit(app, file, line, text):
    fname = os.path.join(app, file)
    source = open(fname, 'r')
    lines = source.readlines()
    lines[line-1] = '%s\n' % text
    source.close()
    source = open(fname, 'w')
    source.write(''.join(lines))
    source.close()
    os.utime(fname, None)

def insert(app, file, line, text):
    fname = os.path.join(app, file)
    source = open(fname, 'r')
    lines = source.readlines()
    lines[line-1:line-1] = '%s\n' % text
    source.close()
    source = open(fname, 'w')
    source.write(''.join(lines))
    source.close()
    os.utime(fname, None)

def create(app, file):
    fname = os.path.join(app, file)
    source = open(fname, 'w')
    source.close()
    os.utime(fname, None)

def delete(app, file, line):
    fname = os.path.join(app, file)
    source = open(fname, 'r')
    lines = source.readlines()
    del lines[line-1]
    source.close()
    source = open(fname, 'w')
    source.write(''.join(lines))
    source.close()
    os.utime(fname, None)    

def rename(app, fro, to):
    os.rename(os.path.join(app, fro), os.path.join(app, to))

if __name__ == '__main__':
    playScript = sys.argv[1]
    sys.argv = [sys.argv[0]]
    unittest.main()
