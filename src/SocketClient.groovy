import java.nio.file.Files
import java.nio.file.Paths

class SocketClient {
	
	public final static int SOCKET_PORT = 13267
	public final static String SERVER = "127.0.0.1"
	public final static String FILE_TO_RECEIVED = "c:/file.txt"
	public final static int FILE_SIZE = 5 * 1024 * 1024
	
	static void main(String[] args) throws IOException {
		int byteLido
		int byteAtual = 0
		FileOutputStream fileOutputStream = null
		BufferedOutputStream bufferedOutputStream = null
		Socket socket = null
		try {
			socket = new Socket(SERVER, SOCKET_PORT)
			println("Conectando...")
			
			byte[] bytes = new byte[FILE_SIZE]
			InputStream is = socket.getInputStream()
			Files.write(Paths.get(FILE_TO_RECEIVED), new byte[0])
			bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(FILE_TO_RECEIVED))
			byteLido = is.read(bytes, byteAtual, bytes.length)
			while (byteLido > -1) {
				byteAtual += byteLido
				byteLido = is.read(bytes, byteAtual, (bytes.length - byteAtual))
			}
			
			bufferedOutputStream.write(bytes, 0, byteAtual)
			bufferedOutputStream.flush()
			println("Arquivo $FILE_TO_RECEIVED baixado ($byteAtual bytes lidos)")
		}
		finally {
			if (fileOutputStream != null) {
				fileOutputStream.close()
			}
			if (bufferedOutputStream != null) {
				bufferedOutputStream.close()
			}
			if (socket != null) {
				socket.close()
			}
		}
	}
	
}