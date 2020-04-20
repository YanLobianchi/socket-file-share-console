import java.nio.file.Files
import java.nio.file.Paths

class SocketServer {

	public static final long MEGABYTE = 1024 * 1024
	public static final int UDP_MAX = 65507
	public static final int TCP_PORT = 13267
	public static final int UDP_PORT = 12345
	public final static String DESKTOP_PATH = "${System.getProperty('user.dir')}\\"
	public final static int FILE_SIZE = 5 * 1024 * 1024

	static void main(String[] args) throws IOException {
		BufferedReader input = System.in.newReader()
		println("1. TCP.\n" +
				"2. UDP.\n" +
				"0. Sair.")
		int protocolo = input.readLine().toInteger()
		while (protocolo != 0) {
			switch (protocolo) {
				case 1:
					socketTCP(input)
					break
				case 2:
					socketUDP(input)
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

	static void socketTCP(final BufferedReader input = System.in.newReader()) throws IOException {
		BufferedInputStream bufferedInputStream
		OutputStream outputStream
		String caminhoDoArquivo
		ServerSocket serverSocket
		Socket socket
		byte[] bytes

		try {
			serverSocket = new ServerSocket(TCP_PORT)

			println("1. Enviar arquivo ao Cliente.\n" +
					"2. Receber arquivo do Cliente.\n" +
					"0. Sair")
			int opcao = input.readLine().toInteger()

			while (opcao != 0) {
				if (opcao == 1) {
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
						bufferedInputStream = new BufferedInputStream(fis)
						bufferedInputStream.read(bytes, 0, bytes.length)
						println("Conectando...")
						socket = serverSocket.accept()
						println("Conectado.")
						outputStream = socket.getOutputStream()
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						outputStream.write(bytes, 0, bytes.length)
						outputStream.flush()
						bufferedInputStream.close()
						outputStream.close()

					} catch (Exception ex) {
						System.err.println("Impossível enviar o arquivo.")
						ex.printStackTrace()
					}
				} else if (opcao == 2) {
					int byteLido
					int byteAtual = 0
					FileOutputStream fileOutputStream = null
					BufferedOutputStream bufferedOutputStream = null
					String nomeArquivo

					try {
						println("Conectando...")
						socket = serverSocket.accept()
						println("Conectado.")
						ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())
						nomeArquivo = objectInputStream.readUTF()
						if (!nomeArquivo) {
							println("1. Enviar arquivo ao Cliente.\n" +
									"2. Receber arquivo do Cliente.\n" +
									"0. Sair")
							opcao = input.readLine().toInteger()
							continue
						}

						println("Conectando...")
						socket = serverSocket.accept()
						println("Conectado.")

						bytes = new byte[FILE_SIZE]
						InputStream inputStream = socket.getInputStream()
						Files.write(Paths.get("$DESKTOP_PATH$nomeArquivo"), new byte[FILE_SIZE])
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
						println("Arquivo $nomeArquivo baixado ($byteAtual bytes lidos) será no diretório atual")
					} finally {
						if (fileOutputStream) {
							fileOutputStream.close()
						}
						if (bufferedOutputStream) {
							bufferedOutputStream.close()
						}
						if (socket && !socket.isClosed()) {
							socket.close()
						}
					}
				}
				println("Feito.\n")
				println("1. Enviar arquivo ao Cliente.\n" +
						"2. Receber arquivo do Cliente.\n" +
						"0. Sair")
				opcao = input.readLine().toInteger()
			}

		}
		finally {
			if (serverSocket) {
				serverSocket.close()
			}
			if (bufferedInputStream) {
				bufferedInputStream.close()
			}
			if (outputStream) {
				outputStream.close()
			}
			if (socket && !socket.isClosed()) {
				socket.close()
			}
		}
	}

	static void socketUDP(final BufferedReader input = System.in.newReader()) throws IOException {
		DatagramSocket datagramSocket
		DatagramPacket datagramPacket
		InetAddress ip = InetAddress.getLocalHost()
		BufferedInputStream inputStream
		OutputStream outputStream
		String caminhoDoArquivo
		byte[] bytes

		try {
			datagramSocket = new DatagramSocket()

			println("1. Enviar arquivo ao Cliente.\n" +
					"2. Receber arquivo do Cliente.\n" +
					"0. Sair")
			int opcao = input.readLine().toInteger()

			while (opcao) {
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
					byte[] bytesNomeArquivo = caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/)
							.getBytes()
					println("Conectando...")
					datagramPacket = new DatagramPacket(bytesNomeArquivo, bytesNomeArquivo.length, ip, UDP_PORT)
					println("Conectado.")

					// envia nome do arquivo
					datagramSocket.send(datagramPacket)

					// envia arquivo
					bytes = new byte[(int) file.length()]
					FileInputStream fis = new FileInputStream(file)
					inputStream = new BufferedInputStream(fis)
					inputStream.read(bytes, 0, bytes.length)

					if (bytes.length > UDP_MAX) {
						int j = 1
						for (int i = 0; !(i > bytes.length); i += UDP_MAX) {
							int total = Math.min(i + UDP_MAX, bytes.length)
							byte[] tamanho = String.valueOf(total - i).getBytes("UTF-8")
							datagramPacket = new DatagramPacket(tamanho, tamanho.length, ip, UDP_PORT)
							datagramSocket.send(datagramPacket)

							byte[] byteAux = Arrays.copyOfRange(bytes, i, total)
							println("Enviando $j partição do arquivo $caminhoDoArquivo ($total bytes)")
							datagramPacket = new DatagramPacket(byteAux, byteAux.length, ip, UDP_PORT)
							datagramSocket.send(datagramPacket)
							j += 1
						}
					} else {
						datagramPacket = new DatagramPacket(bytes, bytes.length, ip, UDP_PORT)
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						datagramSocket.send(datagramPacket)
					}
				} catch (Exception ex) {
					System.err.println("Impossível enviar o arquivo.")
					ex.printStackTrace()
				}
				println("Feito.")

				println("1. Enviar arquivo ao Cliente.\n" +
						"2. Receber arquivo do Cliente.\n" +
						"0. Sair")
				opcao = input.readLine().toInteger()
			}
		}
		finally {
			if (inputStream) {
				inputStream.close()
			}
			if (outputStream) {
				outputStream.close()
			}
			if (datagramSocket && !datagramSocket.isClosed()) {
				datagramSocket.close()
			}
		}
	}
}
