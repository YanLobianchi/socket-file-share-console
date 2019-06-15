class SocketServer {
	
	public static final long MEGABYTE = 1024 * 1024
	public final static int SOCKET_PORT = 13267
	
	static void main(String[] args) throws IOException {
		BufferedInputStream inputStream = null
		OutputStream outputStream = null
		ServerSocket serverSocket = null
		Socket socket = null
		def input = System.in.newReader()
		
		try {
			serverSocket = new ServerSocket(SOCKET_PORT)
			
			println("1. Escolher arquivo para enviar.\n" +
					"0. Sair")
			boolean continuar = input.readLine().toInteger() as boolean
			while (continuar) {
				byte[] bytes = new byte[5 * MEGABYTE]
				String caminhoDoArquivo = ""
				try {
					println("Insira o caminho do arquivo (de até 5MB): ")
					caminhoDoArquivo = input.readLine().replaceAll(/([^(\\|\/).+]?)([\\\/])([^(\\|\/).+]?)/, '\\')
					File file = new File(caminhoDoArquivo)
					while (!file.exists() || file.length() > 5 * MEGABYTE) {
						println("Arquivo inexistente ou tamanho maior que o limite.\n" +
								"Insira um caminho de um arquivo válido: ")
						caminhoDoArquivo = input.readLine()
						file = new File(caminhoDoArquivo)
					}
					Closure enviarNomeDoArquivo = { final Socket socketClosure ->
						ObjectOutputStream saida = new ObjectOutputStream(socketClosure.getOutputStream())
						saida.flush()
						saida.writeUTF(caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/))
						saida.close()
					}
					conectarEEnviarInformacaoAoClient(socket, serverSocket, enviarNomeDoArquivo)
					
					Closure enviarArquivo = { final Socket socketClosure ->
						bytes = new byte[(int) file.length()]
						FileInputStream fis = new FileInputStream(file)
						inputStream = new BufferedInputStream(fis)
						inputStream.read(bytes, 0, bytes.length)
						outputStream = socketClosure.getOutputStream()
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						outputStream.write(bytes, 0, bytes.length)
						outputStream.flush()
						inputStream.close()
						outputStream.close()
					}
					conectarEEnviarInformacaoAoClient(socket, serverSocket, enviarArquivo)
				} catch (Exception ex) {
					System.err.println("Impossível enviar o arquivo.")
					ex.printStackTrace()
				}
				println("Feito.")
				
				println("1. Escolher arquivo para enviar.\n" +
						"0. Sair")
				continuar = input.readLine().toInteger() as boolean
			}
		}
		finally {
			if (serverSocket) {
				serverSocket.close()
			}
			if (inputStream) {
				inputStream.close()
			}
			if (outputStream) {
				outputStream.close()
			}
			if (socket) {
				socket.close()
			}
		}
	}
	
	private static void conectarEEnviarInformacaoAoClient(Socket socket, ServerSocket serverSocket, Closure closure) {
		try {
			println("Conectando...")
			socket = serverSocket.accept()
			println("Conectado.")
			closure.call(socket)
		} catch (Exception ex) {
			System.err.println("Impossível estabelecer conexão com a porta.")
			ex.printStackTrace()
		} finally {
			if (socket) {
				socket.close()
			}
		}
	}
}
