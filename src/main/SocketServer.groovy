class SocketServer {
	
	public static final long MEGABYTE = 1024 * 1024
	public static final int SOCKET_PORT = 13267
	
	static void main(String[] args) throws IOException {
		BufferedInputStream inputStream
		OutputStream outputStream
		ServerSocket serverSocket
		Socket socket
		BufferedReader input = System.in.newReader()
		String caminhoDoArquivo
		byte[] bytes
		
		try {
			serverSocket = new ServerSocket(SOCKET_PORT)
			
			println("1. Escolher arquivo para enviar.\n" +
					"0. Sair")
			boolean continuar = input.readLine().toInteger() as boolean
			
			while (continuar) {
				try {
					println("Insira o caminho do arquivo (de até 5MB): ")
					caminhoDoArquivo = input.readLine().replaceAll(/([^(\\{1,2}).+]?)\\{1,2}([^(\\{1,2}).+]?)/, /$1\/$2/)
					File file = new File(caminhoDoArquivo)
					while (!file.exists() || file.length() > 5 * MEGABYTE) {
						println("Arquivo inexistente ou tamanho maior que o limite.\n" +
								"Insira um caminho de um arquivo válido: ")
						caminhoDoArquivo = input.readLine()
						file = new File(caminhoDoArquivo)
					}
					
					println("Conectando...")
					socket = serverSocket.accept()
					println("Conectado.")
					
					// envia nome do arquivo
					ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())
					saida.flush()
					saida.writeUTF(caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/))
					saida.close()
					
					// envia arquivo
					bytes = new byte[(int) file.length()]
					FileInputStream fis = new FileInputStream(file)
					inputStream = new BufferedInputStream(fis)
					inputStream.read(bytes, 0, bytes.length)
					outputStream = socket.getOutputStream()
					println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
					outputStream.write(bytes, 0, bytes.length)
					outputStream.flush()
					inputStream.close()
					outputStream.close()
					
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
			if (socket && !socket.isClosed()) {
				socket.close()
			}
		}
	}
}
