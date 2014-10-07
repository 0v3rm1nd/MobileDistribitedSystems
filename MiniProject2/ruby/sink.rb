require 'socket'

socket = TCPSocket.new 'localhost', 7008

while line = socket.gets
	puts line
end
socket.close