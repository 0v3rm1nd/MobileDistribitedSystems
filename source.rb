require 'socket'

socket = TCPSocket.new 'localhost', 7007

while line = STDIN.gets
	socket.puts line
end
socket.close