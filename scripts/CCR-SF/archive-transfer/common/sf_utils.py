import logging
import os
import re
import subprocess

from metadata.sf_helper import SFHelper


class SFUtils(object):


    @staticmethod
    def path_contains_exclude_str(tarfile_name, path):
        exclusion_list = ['10X', 'Phix', 'PhiX', 'demux', 'demultiplex']
        if any(ext in path for ext in exclusion_list):
            SFUtils.record_exclusion(tarfile_name, path.rstrip(), 'Path contains substring from exclusion list')
            return True
        return False



    @staticmethod
    def get_meta_path(filepath, log = True):

        path = filepath.replace("uploads/", "")
        path = re.sub(r'.*Unaligned[^/]*/', '', path)

        # strip 'Project_' if it exists
        path = path.replace("Project_", "")

        if log is True:
            logging.info('metadata base: ' + path)

        return path


    @staticmethod
    def record_exclusion(tarfile_name, file_name, str):

        excludes = open("excluded_files", "a")
        excludes.writelines(tarfile_name + ": " + file_name + " - " + str + '\n')
        excludes.close()

        excludes_csv = open("sf_excluded.csv", "a")
        excludes_csv.write(tarfile_name + ", " + file_name + ", " + str + "\n")
        excludes_csv.close()

        logging.warning('Ignoring file ' + str)


    @staticmethod
    def extract_files_from_tar(tarfile_path, extract_path):

        # extract files from the archive
        command = "tar -xf " + tarfile_path + " -C " + extract_path
        logging.info(command)
        os.system(command)
        logging.info("Extracted tarball " + tarfile_path)



    @staticmethod
    def get_filepath_to_archive(line, extract_path):
        # Remove the ../ from the path in the list - TBD - Confirm that all content list files have it like that ?
        filepath = line.rstrip().split("../")[-1]
        filepath = filepath.split(" ")[-1]
        filepath = filepath.lstrip('/')

        filepath = extract_path + filepath

        logging.info("file to archive: " + filepath)
        return filepath



    @staticmethod
    def get_tarball_contents(tarfile_name, tarfile_dir):

        logging.info("Getting contents for: " + tarfile_name)
        tarfile_name = tarfile_name.rstrip()

        if '10x' in tarfile_name or 'singlecell' in tarfile_name or 'lane123456' in tarfile_name:
            excludes_str = ': Invalid tar file -  10x or singlecell'
            SFUtils.record_exclusion(tarfile_name, "All files", excludes_str)
            return


        if not tarfile_name.endswith('tar.gz') and not tarfile_name.endswith('tar'):

            # If this is not a .list, _archive.list, or .md5 file also, then record exclusion. Else
            # just ignore, do not record because we may find the associated tar later
            if (not tarfile_name.endswith('.list') and
                not tarfile_name.endswith('list.txt') and
                not tarfile_name.endswith('.md5')):
                excludes_str = ': Invalid file format - not tar.gz or tar.gz.md5 or acceptable content list format'
                SFUtils.record_exclusion(tarfile_name, "All files", excludes_str)
            else:
                logging.info(tarfile_name + ': No contents to extract')
            return


        tarfile_path = tarfile_dir + '/' + tarfile_name
        contentFiles = [tarfile_path + '.list', tarfile_name + '.list', tarfile_path + '_archive.list',
                    tarfile_path.split('.gz')[0] + '.list', tarfile_path.split('.tar')[0] + '.list',
                    tarfile_path.split('.tar')[0] + '_archive.list', tarfile_path.split('.tar')[0] + '.archive.list',
                    tarfile_path.split('.gz')[0] + '.list.txt', tarfile_path.split('.tar')[0] + '.list.txt',
                    tarfile_path.split('.gz')[0] + '_list.txt', tarfile_path.split('.tar')[0] + '_list.txt',
                    tarfile_path.split('.tar')[0] + '_file_list.txt']

        tarfile_contents = None

        for filename in contentFiles:
            if os.path.exists(filename):
                tarfile_contents = open(filename)
                break

        if tarfile_contents is None:
            command = "tar tvf " + tarfile_path + " > " + tarfile_name + ".list"
            # os.system(command)
            subprocess.call(command, shell=True)
            logging.info("Created contents file: " + command)
            tarfile_contents = open(tarfile_name + '.list')

        logging.info("Obtained contents for: " + tarfile_name)
        return tarfile_contents



    # Record to csv file: tarfile name, file path, archive path
    @staticmethod
    def record_to_csv(tarfile_name, filepath, fullpath, archive_path):

        flowcell_id = SFHelper.get_flowcell_id(tarfile_name)
        normalized_filepath = fullpath.split("uploads/")[-1]
        if filepath.endswith('html'):
            head, sep, tail = fullpath.partition('all/')
            path = head.split(flowcell_id + '/')[-1]
        else:
            path = SFUtils.get_meta_path(fullpath, False)

        includes_csv = open("sf_included.csv", "a")
        includes_csv.write(tarfile_name + ", " + normalized_filepath + ", " + archive_path + ", " + flowcell_id +
            ", " + SFHelper.get_pi_name(path) + ", " + SFHelper.get_project_id(path) +
            ", " + SFHelper.get_project_name(path, tarfile_name) + ", " + SFHelper.get_sample_name(path) +
            ", " + SFHelper.get_run_name(tarfile_name) + ", " + SFHelper.get_sequencing_platform(tarfile_name)  + "\n")
        includes_csv.close()