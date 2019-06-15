import java.nio.file.Files
import java.nio.file.Paths

class SocketClient {
	
	public final static int SOCKET_PORT = 13267
	public final static String SERVER = "127.0.0.1"
	public final static String DESKTOP_PATH = "${System.getProperty("user.home")}\\Desktop\\"
	public final static int FILE_SIZE = 5 * 1024 * 1024
	
	static void main(String[] args) throws IOException {
		
		def input = System.in.newReader()
		boolean continuar = true
		boolean conectou = true
		
		while (continuar && conectou) {
			int byteLido
			int byteAtual = 0
			FileOutputStream fileOutputStream = null
			BufferedOutputStream bufferedOutputStream = null
			String nomeArquivo = ""
			
			try {
				boolean sucesso = conectarEReceberInformacao { Socket socket ->
					ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
					nomeArquivo = entrada.readUTF()
				}
				
				if (!nomeArquivo || !sucesso) {
					println("Continuar recebendo? (1 = sim, 0 = não)")
					continuar = input.readLine().toInteger() as boolean
					continue
				}
				
				conectarEReceberInformacao { final Socket socket ->
					byte[] bytes = new byte[FILE_SIZE]
					InputStream inputStream = socket.getInputStream()
					Files.write(Paths.get("$DESKTOP_PATH$nomeArquivo"), new byte[0])
					bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("$DESKTOP_PATH$nomeArquivo"))
					byteLido = inputStream.read(bytes, byteAtual, (bytes.length - byteAtual))
					while (byteLido > -1) {
						byteAtual += byteLido
						byteLido = inputStream.read(bytes, byteAtual, (bytes.length - byteAtual))
					}
					
					bufferedOutputStream.write(bytes, 0, byteAtual)
					bufferedOutputStream.flush()
					bufferedOutputStream.close()
					inputStream.close()
					println("Arquivo $nomeArquivo baixado ($byteAtual bytes lidos) será salvo na área de trabalho")
				}
				
				println("Continuar recebendo? (1 = sim, 0 = não)")
				continuar = input.readLine().toInteger() as boolean
			} finally {
				if (fileOutputStream) {
					fileOutputStream.close()
				}
				if (bufferedOutputStream) {
					bufferedOutputStream.close()
				}
			}
		}
	}
	
	private static boolean conectarEReceberInformacao(Closure closure) {
		try {
			println("Conectando...")
			final Socket socket = new Socket(SERVER, SOCKET_PORT)
			println("Conectado.")
			closure.call(socket)
			socket.close()
			return true
		} catch (Exception ex) {
			System.err.println("Impossível estabelecer conexão com a porta do servidor.")
			ex.printStackTrace()
			return false
		}
	}
	
}