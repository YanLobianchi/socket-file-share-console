import java.nio.file.Files
import java.nio.file.Paths

class SocketClient {
	
	public final static int TCP_PORT = 13267
	public final static int UDP_PORT = 12345
	public final static String SERVER = "127.0.0.1"
	public final static String DESKTOP_PATH = "${System.getProperty("user.home")}/Desktop/"
	public final static int FILE_SIZE = 5 * 1024 * 1024
	public final static int FILE_SIZE_UDP = 65507
	
	static void main(String[] args) throws IOException {
		BufferedReader input = System.in.newReader()
		println("1. TCP.\n" +
				"2. UDP.\n" +
				"0. Sair.")
		int protocolo = input.readLine().toInteger()
		while (protocolo != 0) {
			switch (protocolo) {
				case 1:
					clientTCP(input)
					break
				case 2:
					clientUDP(input)
					break
				default:
					println("Opção inválida. Escolha novamente.")
			}
			
			println("1. TCP.\n" +
					"2. UDP.\n" +
					"0. Sair.")
			protocolo = input.readLine().toInteger()
		}
	}
	
	static void clientTCP(final BufferedReader input = System.in.newReader()) throws IOException {
		boolean continuar = true
		while (continuar) {
			int byteLido
			int byteAtual = 0
			FileOutputStream fileOutputStream = null
			BufferedOutputStream bufferedOutputStream = null
			String nomeArquivo = ""
			
			try {
				final boolean sucesso = receberBufferTCP { Socket socket ->
					ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
					nomeArquivo = entrada.readUTF()
				}
				
				if (!nomeArquivo || !sucesso) {
					println("Continuar recebendo? (1 = sim, 0 = não)")
					continuar = input.readLine().toInteger() as boolean
					continue
				}
				
				receberBufferTCP { final Socket socket ->
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
	
	static void clientUDP(final BufferedReader input = System.in.newReader()) throws IOException {
		boolean continuar = true
		DatagramSocket datagramSocket = new DatagramSocket(UDP_PORT)
		DatagramPacket datagramPacket
		
		while (continuar) {
			byte[] bytesNomeArquivo = new byte[65535]
			try {
				datagramPacket = new DatagramPacket(bytesNomeArquivo, bytesNomeArquivo.length)
				datagramSocket.receive(datagramPacket)
				
				String nomeArquivo = bytesToString(bytesNomeArquivo)
				
				if (!nomeArquivo) {
					println("Continuar recebendo? (1 = sim, 0 = não)")
					continuar = input.readLine().toInteger() as boolean
					continue
				}
				
				byte[] bytes = new byte[FILE_SIZE]
				datagramPacket = new DatagramPacket(bytes, bytes.length)
				datagramSocket.receive(datagramPacket)
				Files.write(Paths.get("$DESKTOP_PATH$nomeArquivo"), bytes)
				println("Arquivo $nomeArquivo baixado (${bytes.length} bytes lidos) será salvo na área de trabalho")
				
				println("Continuar recebendo? (1 = sim, 0 = não)")
				continuar = input.readLine().toInteger() as boolean
			} catch (Exception ex) {
				System.err.println("Impossível estabelecer conexão com a porta do servidor.")
				ex.printStackTrace()
			}
		}
	}
	
	private static boolean receberBufferTCP(Closure closure) {
		try {
			println("Conectando...")
			final Socket socket = new Socket(SERVER, TCP_PORT)
			println("Conectado.")
			closure.call(socket)
			socket.close()
			socket.finalize()
			return true
		} catch (Exception ex) {
			System.err.println("Impossível estabelecer conexão com a porta do servidor.")
			ex.printStackTrace()
			return false
		}
	}
	
	static StringBuilder bytesToString(byte[] a) {
		if (a == null) {
			return null
		}
		StringBuilder ret = new StringBuilder()
		for (int i = 0; a[i] != 0 as byte; i++) {
			ret.append((char) a[i])
		}
		return ret
	}
}