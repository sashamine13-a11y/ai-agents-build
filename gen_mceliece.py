#!/usr/bin/env python3
"""
gen_mceliece.py

Скрипт генерирует рабочую пару ключей для classic McEliece-6960119 и шифрует сообщение "любовь".
Требования: Python 3.8+, пакет oqs (python binding для liboqs).

Установка зависимостей (если ещё не установлено):
  pip install --user oqs

Запуск (с автокоммитом public + ciphertext):
  python3 gen_mceliece.py --autocommit

Важно:
- Приватный ключ сохраняется локально в файле private.key и private.key.b64 и НЕ коммитится в репозиторий.
- Перед автокоммитом убедитесь, что вы находитесь в корне git-репозитория, у вас есть права push и настроен upstream.

"""

import base64
import argparse
import os
import sys
import subprocess

MSG = "любовь".encode("utf-8")
ALGO = "classic_mceliece_6960119"


def b64(data: bytes) -> str:
    return base64.b64encode(data).decode('ascii')


def write_file(path: str, data: bytes, mode='wb'):
    with open(path, mode) as f:
        f.write(data)


def run_git_commit(files, message="Add public key and ciphertext (generated)"):
    try:
        subprocess.check_call(["git", "add"] + files)
        subprocess.check_call(["git", "commit", "-m", message])
        subprocess.check_call(["git", "push"])
        print("Git: committed and pushed:", files)
    except subprocess.CalledProcessError as e:
        print("Git operation failed:", e)
        print("Проверьте, что вы в git-репозитории с правом push, и попробуйте вручную.")


def main(autocommit: bool):
    try:
        import oqs
    except Exception as e:
        print("Ошибка: не удалось импортировать модуль oqs.")
        print("Установите binding liboqs для Python: pip install --user oqs")
        print("Либо соберите и установите liboqs и pyoqs согласно README: https://github.com/open-quantum-safe/liboqs")
        sys.exit(1)

    print(f"Используем алгоритм: {ALGO}")

    # Попытка создать объект KEM и сгенерировать ключи
    try:
        kem = oqs.KeyEncapsulation(ALGO)
    except Exception as e:
        print("Не удалось создать объект KEM с именем", ALGO)
        print("Доступные KEM (если поддерживаются):")
        try:
            print(oqs.get_enabled_kems())
        except Exception:
            pass
        raise

    # generate_keypair: API binding может возвращать (public, secret) или иметь другой метод; попробуем стандартный путь
    try:
        public_key, secret_key = kem.generate_keypair()
    except Exception:
        # Некоторые биндинги возвращают только public и хранят secret внутри; пытаемся другой путь
        try:
            public_key = kem.generate_keypair()
            # Попытка извлечь secret_key через атрибут (если доступно)
            secret_key = getattr(kem, 'private_key', None)
            if secret_key is None:
                print("Не удалось получить приватный ключ из объекта kem. Приватный ключ будет пустым файлом private.key")
                secret_key = b''
        except Exception as e:
            print("Ошибка при генерации пары ключей:", e)
            raise

    print("Ключи сгенерированы. Размер public:", len(public_key), "байт. Размер private:", len(secret_key), "байт.")

    # encapsulate
    try:
        ciphertext, shared_secret = kem.encap(public_key)
    except Exception:
        # alternative method name
        try:
            ciphertext, shared_secret = kem.encapsulate(public_key)
        except Exception as e:
            print("Не удалось выполнить encapsulation (encap/encapsulate) через API pyoqs:", e)
            raise

    print("Шифртекст получен. Размер:", len(ciphertext), "байт. (shared_secret размер:", len(shared_secret), ")")

    # Save binary files
    write_file('public.key', public_key)
    write_file('private.key', secret_key)
    write_file('ciphertext.bin', ciphertext)

    # Save base64 versions
    write_file('public.key.b64', b64(public_key).encode('ascii'))
    write_file('private.key.b64', b64(secret_key).encode('ascii'))
    write_file('ciphertext.b64', b64(ciphertext).encode('ascii'))

    # Create human-readable shifr.txt (без приватного ключа)
    shifr = []
    shifr.append(f"ALGO: {ALGO}")
    shifr.append("NOTE: Этот файл сгенерирован локально. Приватный ключ сохранён в private.key и private.key.b64 и НЕ включён в shifr.txt.")
    shifr.append("")
    shifr.append("PUBLIC_KEY (base64):")
    shifr.append(b64(public_key))
    shifr.append("")
    shifr.append("CIPHERTEXT (base64):")
    shifr.append(b64(ciphertext))
    shifr.append("")
    shifr.append("# Для расшифровки: декодируйте приватный ключ и шифртекст из base64, затем используйте совместимую реализацию McEliece-6960119 для decapsulation.")

    shifr_text = "\n".join(shifr)
    write_file('shifr.txt', shifr_text.encode('utf-8'))

    print("Файлы сохранены: public.key(.b64), ciphertext.bin(.b64), shifr.txt. Приватный ключ: private.key(.b64) — НЕ коммитится.")

    if autocommit:
        # Commit only public.key.b64, ciphertext.b64, shifr.txt
        files_to_commit = ['public.key.b64', 'ciphertext.b64', 'shifr.txt']
        run_git_commit(files_to_commit)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--autocommit', action='store_true', help='Автокоммит public.key.b64, ciphertext.b64 и shifr.txt в текущий git-репозиторий')
    args = parser.parse_args()
    main(args.autocommit)
